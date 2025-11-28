package com.beyond.synclab.ctrlline.domain.productionperformance.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateProductionPerformanceRequestDto {

    // remark만 수정 가능
    private String remark;

    public static UpdateProductionPerformanceRequestDto of(String remark) {
        return UpdateProductionPerformanceRequestDto.builder()
                .remark(remark)
                .build();
    }
}
