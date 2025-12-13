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
        ORDER BY pp.createdAt DESC
    """)
    List<String> findByDocumentNoByPrefix(@Param("prefix") String prefix);

    Optional<ProductionPlans> findFirstByDocumentNoOrderByIdDesc(String documentNo);

    Optional<ProductionPlans> findFirstByDocumentNoAndStatusOrderByIdDesc(String documentNo, PlanStatus status);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductionPlans p SET p.status = :status WHERE p.id IN :ids")
    int updateAllStatusById(@Param("ids") List<Long> planIds, @Param("status") PlanStatus planStatus);


    @Query("""
    SELECT p
    FROM ProductionPlans p
    WHERE p.itemLine.lineId = :lineId
      AND (:statuses IS NULL OR p.status IN :statuses)
      AND p.endTime > :now
    ORDER BY p.startTime ASC
""")
    List<ProductionPlans> findAllByLineIdAndStatusesOrderByStartTimeAsc(
        @Param("lineId") Long lineId,
        @Param("statuses") List<ProductionPlans.PlanStatus> statuses,
        @Param("now") LocalDateTime now
    );

    List<ProductionPlans> findAllByIdIn(List<Long> ids);

    @Query("""
    SELECT p
    FROM ProductionPlans p
    WHERE p.itemLine.line.lineCode = :lineCode
      AND (:statuses IS NULL OR p.status IN :statuses)
    ORDER BY p.startTime ASC
    """)
    List<ProductionPlans> findAllByLineCodeAndStatusInOrderByStartTimeAsc(String lineCode, List<PlanStatus> statuses);

    @Query("""
    select p
    from ProductionPlans p
    join ItemsLines il on p.itemLineId = il.id
    where il.lineId = :lineId
    and p.startTime >= :startTime
    order by p.startTime asc
""")
    List<ProductionPlans> findAllByLineIdAndStartTimeAfterOrderByStartTimeAsc(Long lineId, @Param("startTime") LocalDateTime scheduledEnd);

    List<ProductionPlans> findAllByStatus(PlanStatus planStatus);
}
