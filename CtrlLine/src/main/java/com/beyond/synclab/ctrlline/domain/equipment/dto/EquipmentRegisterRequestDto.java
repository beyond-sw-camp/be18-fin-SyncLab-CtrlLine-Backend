package com.beyond.synclab.ctrlline.domain.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EquipmentRegisterRequestDto {

    @NotBlank(message = "설비 코드는 필수입니다.")
    private String equipmentCode;

    @NotBlank(message = "설비명은 필수입니다.")
    private String equipmentName;

    @NotBlank(message = "설비유형은 필수입니다.")
    private String equipmentType;

    @NotNull(message = "PPM 값은 필수입니다.")
    private BigDecimal equipmentPpm;

    @NotNull(message = "사원명은 필수입니다.")
    private String userName;

    @NotNull(message = "담당부서는 필수입니다.")
    private String userDepartment;

    @NotBlank(message = "사원번호는 필수입니다.")
    private String empNo;

    @NotNull(message = "활성화 상태는 필수입니다.")
    private Boolean isActive;
}
