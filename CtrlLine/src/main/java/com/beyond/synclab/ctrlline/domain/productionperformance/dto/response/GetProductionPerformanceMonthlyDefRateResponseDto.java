package com.beyond.synclab.ctrlline.domain.productionperformance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetProductionPerformanceMonthlyDefRateResponseDto {

    // 기준 월
    private String month;

    // 해당 월의 총 불량률 (%)
    private BigDecimal monthlyDefectiveRate;

    public static GetProductionPerformanceMonthlyDefRateResponseDto of(
            String month,
            BigDecimal monthlyDefectiveRate
    ) {
        return GetProductionPerformanceMonthlyDefRateResponseDto.builder()
                .month(month)
                .monthlyDefectiveRate(monthlyDefectiveRate)
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FactoryMonthlyDefectiveRate {

        private String factoryCode;
        private String factoryName;

        @JsonProperty("performances")
        private List<GetProductionPerformanceMonthlyDefRateResponseDto> performances;

        public static FactoryMonthlyDefectiveRate of(
                String factoryCode,
                String factoryName,
                List<GetProductionPerformanceMonthlyDefRateResponseDto> performances
        ) {
            return FactoryMonthlyDefectiveRate.builder()
                    .factoryCode(factoryCode)
                    .factoryName(factoryName)
                    .performances(performances)
                    .build();
        }
    }
}
