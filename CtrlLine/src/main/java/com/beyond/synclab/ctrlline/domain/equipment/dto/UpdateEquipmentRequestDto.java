package com.beyond.synclab.ctrlline.domain.equipment.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)

// 설비에서 변경 가능한 파라미터: 담당자, 사용여부
public class UpdateEquipmentRequestDto {
    private String userName;
    private Boolean isActive;
}
