package com.beyond.synclab.ctrlline.domain.equipment.repository;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipments, Long> {

    Optional<Equipments> findByEquipmentCode(String equipmentCode);

    // 설비코드 중복 여부 확인
    boolean existsByEquipmentCode(String equipmentCode);

    // 페이지네이션
    Page<Equipments> findByEquipmentCode(String equipmentCode, Pageable pageable);
}
