package com.beyond.synclab.ctrlline.domain.productionperformance.repository.query;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface ProductionPerformanceMonthlyDefRateQueryRepository {

    Map<YearMonth, MonthlyQtySum> getMonthlyQtySum(String factoryCode, List<YearMonth> months);

    // 월별 투입·실적 수량 묶음 DTO
    class MonthlyQtySum {
        private final BigDecimal totalQtySum;
        private final BigDecimal performanceQtySum;

        public MonthlyQtySum(BigDecimal totalQtySum, BigDecimal performanceQtySum) {
            this.totalQtySum = totalQtySum;
            this.performanceQtySum = performanceQtySum;
        }

        public BigDecimal getTotalQtySum() {
            return totalQtySum;
        }

        public BigDecimal getPerformanceQtySum() {
            return performanceQtySum;
        }
    }
}
