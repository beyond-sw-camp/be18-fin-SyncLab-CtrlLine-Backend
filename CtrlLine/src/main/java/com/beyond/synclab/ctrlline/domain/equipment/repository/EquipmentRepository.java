package com.beyond.synclab.ctrlline.domain.equipment.repository;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
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
}
