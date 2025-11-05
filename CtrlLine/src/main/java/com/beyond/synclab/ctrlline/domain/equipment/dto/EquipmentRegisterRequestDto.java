package com.beyond.synclab.ctrlline.domain.equipment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class EquipmentRegisterRequestDto {
    private String equipmentCode;
    private String equipmentName;
    private String equipmentType;
    private BigDecimal equipmentPpm;
    private String userName;
    private String userDepartment;
    private String empNo;

    @JsonProperty("is_active")
    private Boolean isActive;
}
