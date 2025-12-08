package com.beyond.synclab.ctrlline.domain.equipment.service.dto;

public record EquipmentLocation(
        Long equipmentId,
        String equipmentCode,
        Long lineId,
        String lineCode,
        Long factoryId,
        String factoryCode
) {
}
