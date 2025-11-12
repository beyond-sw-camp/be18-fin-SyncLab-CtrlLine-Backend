package com.beyond.synclab.ctrlline.domain.equipment.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class EquipmentListResponseDto {
    private String equipmentCode;
    private String equipmentType;
    private String userName;
    private String userDepartment;
}
