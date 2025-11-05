package com.beyond.synclab.ctrlline.domain.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRegisterResponseDto {
        private String equipmentCode;
        private String equipmentName;
        private String equipmentType;
        private BigDecimal equipmentPpm;
        private String userName;
        private String userDepartment;
        private String empNo;
        private Boolean isActive;
}
