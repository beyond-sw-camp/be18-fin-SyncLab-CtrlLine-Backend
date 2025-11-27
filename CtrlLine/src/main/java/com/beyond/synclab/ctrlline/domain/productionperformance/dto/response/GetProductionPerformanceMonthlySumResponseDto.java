package com.beyond.synclab.ctrlline.domain.productionperformance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetProductionPerformanceMonthlySumResponseDto {

    // 2025-11 같은 형태의 기준 월
    private String month;

    // 해당 월의 총 생산량(누적)
    private Long totalPerformanceQty;

    public static GetProductionPerformanceMonthlySumResponseDto of(
            String month,
            Long totalPerformanceQty
    ) {
        return GetProductionPerformanceMonthlySumResponseDto.builder()
                .month(month)
                .totalPerformanceQty(totalPerformanceQty)
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FactoryMonthlyPerformance {

        private String factoryCode;
        private String factoryName;

        @JsonProperty("performances")
        private List<GetProductionPerformanceMonthlySumResponseDto> performances;

        public static FactoryMonthlyPerformance of(
                String factoryCode,
                String factoryName,
                List<GetProductionPerformanceMonthlySumResponseDto> performances
        ) {
            return FactoryMonthlyPerformance.builder()
                    .factoryCode(factoryCode)
                    .factoryName(factoryName)
                    .performances(performances)
                    .build();
        }
    }
}
