package com.beyond.synclab.ctrlline.domain.equipment.repository;

import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchDto;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EquipmentQueryRepository {
    Page<Equipments> searchEquipmentList(EquipmentSearchDto searchDto, Pageable pageable);
}
