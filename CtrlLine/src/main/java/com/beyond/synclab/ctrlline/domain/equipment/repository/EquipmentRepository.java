package com.beyond.synclab.ctrlline.domain.equipment.repository;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipments, Long>, EquipmentQueryRepository {

    Optional<Equipments> findByEquipmentCode(String equipmentCode);

    // 설비코드 중복 여부 확인
    boolean existsByEquipmentCode(String equipmentCode);

    List<Equipments> findAllByLineId(Long lineId);
}
