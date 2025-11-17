package com.beyond.synclab.ctrlline.domain.equipment.dto;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EquipmentResponseDto {
        private String equipmentCode;
        private String equipmentName;
        private String equipmentType;
        private BigDecimal equipmentPpm;
        private String userName;
        private String userDepartment;
        private String empNo;
        private Boolean isActive;

<<<<<<< HEAD:CtrlLine/src/main/java/com/beyond/synclab/ctrlline/domain/equipment/dto/EquipmentRegisterResponseDto.java
        public static EquipmentRegisterResponseDto fromEntity(Equipments equipment, Users user) {
                return EquipmentRegisterResponseDto.builder()
=======

        public static EquipmentResponseDto fromEntity(Equipments equipment, Users user) {
                return EquipmentResponseDto.builder()
>>>>>>> e16a39c9ce4734a5bb3f7902776e265d18f64ee6:CtrlLine/src/main/java/com/beyond/synclab/ctrlline/domain/equipment/dto/EquipmentResponseDto.java
                        .equipmentCode(equipment.getEquipmentCode())
                        .equipmentName(equipment.getEquipmentName())
                        .equipmentType(equipment.getEquipmentType())
                        .equipmentPpm(equipment.getEquipmentPpm())
                        .isActive(equipment.getIsActive())
                        .userName(user.getName())
                        .userDepartment(user.getDepartment())
                        .empNo(user.getEmpNo())
                        .build();
        }
}
