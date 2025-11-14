package com.beyond.synclab.ctrlline.domain.equipment.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class UpdateEquipmentResponseDto {
    private final String userName;
    private final Boolean isActive;

    // 가독성을 위해, 추가함.
    public static UpdateEquipmentResponseDto of (String userName, Boolean isActive) {
        return UpdateEquipmentResponseDto.builder()
                .isActive(isActive)
                .userName(userName)
                .build();
    }
}