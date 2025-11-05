package com.beyond.synclab.ctrlline.domain.equipment.repository;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

public interface EquipmentRepository extends JpaRepository<Equipments, Long> {

    // 설비코드로 단건 조회
    // Optional<Equipments> findByEquipmentCode(String equipmentCode);

    // 설비코드 중복 여부 확인
    boolean existsByEquipmentCode(String equipmentCode);

    // (선택) 접두사 기반 설비코드 목록 조회 (EQP-0001, EQP-0002, ...)
    // @Query("SELECT e.equipmentCode FROM Equipments e WHERE e.equipmentCode LIKE CONCAT(:prefix, '%') ORDER BY e.equipmentCode DESC")
    // List<String> findEquipmentCodesByPrefix(@Param("prefix") String prefix);
}
