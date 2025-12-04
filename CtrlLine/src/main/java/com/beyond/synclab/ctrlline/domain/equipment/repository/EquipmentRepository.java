package com.beyond.synclab.ctrlline.domain.equipment.repository;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.service.dto.EquipmentLocation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipments, Long>, EquipmentQueryRepository {

    Optional<Equipments> findByEquipmentCode(String equipmentCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Equipments e where e.equipmentCode = :equipmentCode")
    Optional<Equipments> findByEquipmentCodeForUpdate(String equipmentCode);

    // 설비코드 중복 여부 확인
    boolean existsByEquipmentCode(String equipmentCode);

    List<Equipments> findAllByLineId(Long lineId);

    @Query("""
            select new com.beyond.synclab.ctrlline.domain.equipment.service.dto.EquipmentLocation(
                e.id,
                e.equipmentCode,
                l.id,
                l.lineCode,
                f.id,
                f.factoryCode
            )
            from Equipments e
            join e.line l
            join l.factory f
            where e.equipmentCode = :equipmentCode
            """)
    Optional<EquipmentLocation> findLocationByEquipmentCode(String equipmentCode);
}
