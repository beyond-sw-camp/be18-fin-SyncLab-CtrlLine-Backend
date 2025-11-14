package com.beyond.synclab.ctrlline.domain.equipment.dto;

// 설비 목록 조회 응답.

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class EquipmentSearchResponseDto {
        private final String equipmentCode;
        private final String equipmentName;
        private final String equipmentType;
        private final String userDepartment;
        private final String userName;
        private final String empNo;
        private final Boolean isActive;

        public static EquipmentSearchResponseDto fromEntity(Equipments equipment, Users user) {
            return EquipmentSearchResponseDto.builder()
                    .equipmentCode(equipment.getEquipmentCode())
                    .equipmentName(equipment.getEquipmentName())
                    .equipmentType(equipment.getEquipmentType())
                    .userDepartment(user.getDepartment())
                    .userName(user.getName())
                    .empNo(user.getEmpNo())
                    .isActive(equipment.getIsActive())
                    .build();
        }
}
