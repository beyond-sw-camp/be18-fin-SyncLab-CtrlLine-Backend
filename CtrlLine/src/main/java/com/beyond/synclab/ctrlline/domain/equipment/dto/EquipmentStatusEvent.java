package com.beyond.synclab.ctrlline.domain.equipment.dto;

import com.beyond.synclab.ctrlline.domain.equipment.service.dto.EquipmentLocation;
import com.beyond.synclab.ctrlline.domain.equipment.service.dto.EquipmentRuntimeStatusSnapshot;
import java.time.LocalDateTime;

public record EquipmentStatusEvent(
        String equipmentCode,
        EquipmentRuntimeStatusLevel runtimeStatusLevel,
        LocalDateTime updatedAt,
        String factoryCode,
        String lineCode
) {

    public static EquipmentStatusEvent from(EquipmentRuntimeStatusSnapshot snapshot, EquipmentLocation location) {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot must not be null");
        }
        return new EquipmentStatusEvent(
                snapshot.equipmentCode(),
                snapshot.statusLevel(),
                snapshot.updatedAt(),
                location != null ? location.factoryCode() : null,
                location != null ? location.lineCode() : null
        );
    }
}
