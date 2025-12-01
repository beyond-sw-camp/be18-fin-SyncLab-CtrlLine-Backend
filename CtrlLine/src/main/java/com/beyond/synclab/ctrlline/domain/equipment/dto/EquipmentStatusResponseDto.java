package com.beyond.synclab.ctrlline.domain.equipment.dto;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;

public record EquipmentStatusResponseDto(
        String equipmentCode,
        String equipmentName,
        EquipmentRuntimeStatusLevel runtimeStatusLevel
) {
    public static EquipmentStatusResponseDto of(Equipments equipment, EquipmentRuntimeStatusLevel statusLevel) {
        return new EquipmentStatusResponseDto(
                equipment.getEquipmentCode(),
                equipment.getEquipmentName(),
                statusLevel
        );
    }
}
