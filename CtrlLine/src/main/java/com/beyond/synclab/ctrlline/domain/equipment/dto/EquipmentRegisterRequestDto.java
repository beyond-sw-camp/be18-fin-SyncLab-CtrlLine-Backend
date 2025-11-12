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
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EquipmentRegisterRequestDto {

    @NotNull(message = "설비 코드는 필수입니다.")
    private String equipmentCode;

    @NotNull(message = "설비명은 필수입니다.")
    private String equipmentName;

    @NotNull(message = "설비유형은 필수입니다.")
    private String equipmentType;

    @NotNull(message = "PPM 값은 필수입니다.")
    private BigDecimal equipmentPpm;

//    @NotNull(message = "담당부서는 필수입니다.")
//    private String userDepartment;

//    @NotNull(message = "담당자는 필수입니다.")
//    private String userName;

    // 사번으로 담당자, 담당부서 찾음.
    @NotNull(message = "사번은 필수입니다.")
    private String empNo;

    @NotNull(message = "설비 사용 여부는 필수입니다.")
    private Boolean isActive;

    private Long lineId;

    private Long equipmentStatusId;

    private LocalDateTime operatingTime;

    // Post할 때, 아무런 값도 안 넣으면 Null이라고 생각해서, 0으로 기본값 넣어줌.
    public Equipments toEntity(Users users) {
        return Equipments.builder()
                .lineId(this.lineId)                       // ✅ FK 1
                .equipmentStatusId(this.equipmentStatusId) // ✅ FK 2
                .equipmentCode(this.equipmentCode)
                .equipmentName(this.equipmentName)
                .equipmentType(this.equipmentType)
                .operatingTime(this.operatingTime)
                .equipmentPpm(this.equipmentPpm != null ? this.equipmentPpm : BigDecimal.ZERO)
                .totalCount(BigDecimal.ZERO)               // ✅ 기본값
                .defectiveCount(BigDecimal.ZERO)           // ✅ 기본값
                .isActive(this.isActive != null ? this.isActive : true)
                .users(users)
                .build();
    }
}
