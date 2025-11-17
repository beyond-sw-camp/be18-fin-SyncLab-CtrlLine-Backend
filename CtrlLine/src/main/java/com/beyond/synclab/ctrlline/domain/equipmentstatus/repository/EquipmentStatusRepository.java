package com.beyond.synclab.ctrlline.domain.equipmentstatus.repository;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipmentstatus.entity.EquipmentStatuses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentStatusRepository extends JpaRepository<EquipmentStatuses, Long> {
    Optional<EquipmentStatuses> findByequipmentStatusCode(String code);
}
