package com.beyond.synclab.ctrlline.domain.equipment.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateEquipmentActRequestDto {
    private List<Long> equipmentIds;
    private Boolean isActive;
}
