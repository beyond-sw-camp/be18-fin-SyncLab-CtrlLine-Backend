package com.beyond.synclab.ctrlline.domain.productionperformance.repository.query;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface ProductionPerformanceMonthlyQueryRepository {

    Map<YearMonth, Long> getMonthlySum(String factoryCode, List<YearMonth> months);
}
