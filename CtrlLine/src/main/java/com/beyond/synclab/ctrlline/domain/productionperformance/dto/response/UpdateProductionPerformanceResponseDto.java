package com.beyond.synclab.ctrlline.domain.productionperformance.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateProductionPerformanceResponseDto {

    private Long id;
    private String remark;

    public static UpdateProductionPerformanceResponseDto of(Long id, String remark) {
        return UpdateProductionPerformanceResponseDto.builder()
                .id(id)
                .remark(remark)
                .build();
    }
}
