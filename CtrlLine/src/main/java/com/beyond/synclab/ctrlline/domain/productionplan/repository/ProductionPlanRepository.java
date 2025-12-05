package com.beyond.synclab.ctrlline.domain.productionplan.repository;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionPlanRepository extends JpaRepository<ProductionPlans, Long>,
    JpaSpecificationExecutor<ProductionPlans> {

    List<ProductionPlans> findAllByStatusAndStartTimeLessThanEqual(PlanStatus status, LocalDateTime startTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT pp.documentNo
        FROM ProductionPlans pp
        WHERE pp.documentNo LIKE :prefix%
        ORDER BY pp.createdAt
    """)
    List<String> findByDocumentNoByPrefix(@Param("prefix") String prefix);

    Optional<ProductionPlans> findFirstByDocumentNoOrderByIdDesc(String documentNo);

    Optional<ProductionPlans> findFirstByDocumentNoAndStatusOrderByIdDesc(String documentNo, PlanStatus status);

    // lineCode + 상태(PENDING, CONFIRMED) 기준으로 최신 생성된 ProductionPlan 조회
    @Query("""
        SELECT p
        FROM ProductionPlans p
        WHERE p.itemLine.line.lineCode = :lineCode
          AND p.status IN :statuses
          AND p.endTime > :now
        ORDER BY p.createdAt DESC
        LIMIT 1
    """)
    Optional<ProductionPlans> findByLineCodeAndStatusInAndEndTimeAfterOrderByCreatedAtDesc(String lineCode, List<PlanStatus> statuses,
        LocalDateTime now);



    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductionPlans p SET p.status = :status WHERE p.id IN :ids")
    int updateAllStatusById(@Param("ids") List<Long> planIds, @Param("status") PlanStatus planStatus);


    @Query("""
    SELECT p
    FROM ProductionPlans p
    WHERE p.itemLine.lineId = :lineId
      AND (:statuses IS NULL OR p.status IN :statuses)
    ORDER BY p.startTime ASC
""")
    List<ProductionPlans> findAllByLineIdAndStatusesOrderByStartTimeAsc(
        @Param("lineId") Long lineId,
        @Param("statuses") List<ProductionPlans.PlanStatus> statuses
    );

    List<ProductionPlans> findAllByIdIn(List<Long> ids);
}
