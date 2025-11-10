package com.beyond.synclab.ctrlline.domain.equipment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class EquipmentListResponseDto {
    private String equipmentCode;
    private String equipmentType;
    private String userName;
    private String userDepartment;
}
