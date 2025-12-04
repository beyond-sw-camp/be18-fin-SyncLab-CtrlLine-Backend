package com.beyond.synclab.ctrlline.domain.equipment.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateEquipmentActResponseDto {
    private final Boolean isActive;

    public static UpdateEquipmentActResponseDto of(Boolean isActive) {
        return UpdateEquipmentActResponseDto.builder()
                .isActive(isActive)
                .build();
    }
}
