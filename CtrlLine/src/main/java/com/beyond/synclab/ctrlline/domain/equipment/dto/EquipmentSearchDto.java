package com.beyond.synclab.ctrlline.domain.equipment.dto;
// 설비 목록 조회 조건을 담아줌.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor

public class EquipmentSearchDto {
    private final String equipmentCode;
    private final String equipmentName;
    private final String equipmentType;
    private final Boolean isActive;
    private final String userName;
    private final String userDepartment;
}
