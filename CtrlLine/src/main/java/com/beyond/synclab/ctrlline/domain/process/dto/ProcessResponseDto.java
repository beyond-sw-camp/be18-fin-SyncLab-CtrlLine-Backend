package com.beyond.synclab.ctrlline.domain.process.dto;
// 공정 상세 조회 응답 & 공정 업데이트(담당자, 사용여부) 응답

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.process.entity.Processes;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class ProcessResponseDto {
    private final String equipmentCode;
    private final String processCode;
    private final String processName;
    private final String userDepartment;
    private final String userName;
    private final String empNo;
    private final Boolean isActive;
    private final LocalDateTime updatedAt;

    public static ProcessResponseDto fromEntity(Processes process, Equipments equipment, Users user) {
        return ProcessResponseDto.builder()
                .equipmentCode(equipment.getEquipmentCode())
                .processCode(process.getProcessCode())
                .processName(process.getProcessName())
                .userDepartment(user.getDepartment())
                .userName(user.getName())
                .empNo(user.getEmpNo())
                .isActive(process.isActive())
                .updatedAt(process.getUpdatedAt())
                .build();
    }
}