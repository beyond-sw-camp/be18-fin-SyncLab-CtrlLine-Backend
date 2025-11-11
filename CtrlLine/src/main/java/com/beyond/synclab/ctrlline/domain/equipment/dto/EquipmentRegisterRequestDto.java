package com.beyond.synclab.ctrlline.domain.equipment.dto;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EquipmentRegisterRequestDto {

    @NotBlank(message = "설비 코드는 필수입니다.")
    private String equipmentCode;

    @NotBlank(message = "설비명은 필수입니다.")
    private String equipmentName;

    @NotBlank(message = "설비유형은 필수입니다.")
    private String equipmentType;

    @NotNull(message = "PPM 값은 필수입니다.")
    private BigDecimal equipmentPpm;

//    @NotNull(message = "담당부서는 필수입니다.")
//    private String userDepartment;

//    @NotNull(message = "담당자는 필수입니다.")
//    private String userName;

    @NotBlank(message = "사번은 필수입니다.")
    private String empNo;

    @NotNull(message = "설비 사용 여부는 필수입니다.")
    private Boolean isActive;


    public Equipments toEntity (Users users) {
        return Equipments.builder()
                .equipmentCode(this.equipmentCode)
                .equipmentName(this.equipmentName)
                .equipmentType(this.equipmentType)
                .users(users)
                .equipmentPpm(this.equipmentPpm)
                .isActive(this.isActive)
                .build();
    }
}
