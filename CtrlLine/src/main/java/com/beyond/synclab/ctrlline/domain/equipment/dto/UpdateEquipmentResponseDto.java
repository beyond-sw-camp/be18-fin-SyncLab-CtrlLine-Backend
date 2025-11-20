package com.beyond.synclab.ctrlline.domain.equipment.dto;
// 설비 상세 조회 응답 Dto

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipmentstatus.entity.EquipmentStatuses;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class UpdateEquipmentResponseDto {
    private String equipmentCode;
    private String equipmentName;
    private String equipmentType;
    private BigDecimal equipmentPpm;
    private String userDepartment;
    private String userName;
    private String empNo;
    private LocalDateTime operatingDate;
    private LocalDateTime maintenanceDate;
    private BigDecimal totalCount;
    private BigDecimal defectiveCount;
    private Boolean isActive;
    private String equipmentStatusCode;
    private String lineCode;

    public static UpdateEquipmentResponseDto fromEntity(Equipments equipment, Users user, EquipmentStatuses status, Lines line) {
        return UpdateEquipmentResponseDto.builder()
                .equipmentCode(equipment.getEquipmentCode())
                .equipmentName(equipment.getEquipmentName())
                .equipmentType(equipment.getEquipmentType())
                .equipmentPpm(equipment.getEquipmentPpm())
                .userDepartment(user.getDepartment())
                .userName(user.getName())
                .empNo(user.getEmpNo())
                .operatingDate(equipment.getOperatingTime())
                .maintenanceDate(equipment.getMaintenanceHistory())
                .totalCount(equipment.getTotalCount())
                .defectiveCount(equipment.getDefectiveCount())
                .isActive(equipment.getIsActive())
                // 추가
                .equipmentStatusCode(status.getEquipmentStatusCode())
                .lineCode(line.getLineCode())
                .build();
    }
}
