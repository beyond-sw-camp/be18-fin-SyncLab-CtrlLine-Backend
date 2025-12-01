package com.beyond.synclab.ctrlline.domain.equipment.service.dto;

import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRuntimeStatusLevel;
import java.time.LocalDateTime;

public record EquipmentRuntimeStatusSnapshot(
        String equipmentCode,
        String state,
        String alarmLevel,
        boolean alarmActive,
        EquipmentRuntimeStatusLevel statusLevel,
        LocalDateTime updatedAt
) {}
