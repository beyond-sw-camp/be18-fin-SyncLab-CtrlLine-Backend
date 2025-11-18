package com.beyond.synclab.ctrlline.domain.equipment.dto;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipmentstatus.entity.EquipmentStatuses;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;

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
public class CreateEquipmentRequestDto {

    @NotNull(message = "설비 코드는 필수입니다.")
    private String equipmentCode;

    @NotNull(message = "설비명은 필수입니다.")
    private String equipmentName;

    @NotNull(message = "설비유형은 필수입니다.")
    private String equipmentType;

    @NotNull(message = "PPM 값은 필수입니다.")
    private BigDecimal equipmentPpm;

    // 사번으로 담당자, 담당부서 찾음.
    @NotNull(message = "사번은 필수입니다.")
    private String empNo;

    @NotNull(message = "설비 사용 여부는 필수입니다.")
    private Boolean isActive;

    private Long lineId;

    private String equipmentStatus;

    private LocalDateTime operatingTime;

    // Post할 때, 아무런 값도 안 넣으면 Null이라고 생각해서, 0으로 기본값 넣어줌.
    public Equipments toEntity(Users user, Lines line, EquipmentStatuses status) {
        return Equipments.builder()
                .line(line)
                .equipmentStatus(status)
                .equipmentCode(this.equipmentCode)
                .equipmentName(this.equipmentName)
                .equipmentType(this.equipmentType)
                .operatingTime(this.operatingTime)
                .equipmentPpm(this.equipmentPpm != null ? this.equipmentPpm : BigDecimal.ZERO)
                .totalCount(BigDecimal.ZERO)
                .defectiveCount(BigDecimal.ZERO)
                .isActive(this.isActive)
                .user(user)
                .build();
    }
}
