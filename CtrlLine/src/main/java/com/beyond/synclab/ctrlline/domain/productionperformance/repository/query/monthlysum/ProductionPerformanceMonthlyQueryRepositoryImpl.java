package com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.monthlysum;

import com.beyond.synclab.ctrlline.domain.itemline.entity.QItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.QLines;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.QProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.QProductionPlans;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductionPerformanceMonthlyQueryRepositoryImpl
        implements ProductionPerformanceMonthlyQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<YearMonth, Long> getMonthlySum(String factoryCode, List<YearMonth> months) {

        QProductionPerformances perf = QProductionPerformances.productionPerformances;
        QProductionPlans plan = QProductionPlans.productionPlans;
        QItemsLines itemLine = QItemsLines.itemsLines;
        QLines line = QLines.lines;

        // "YYYY-MM" 리스트
        List<String> monthStrings = months.stream()
                .map(YearMonth::toString)
                .toList();

        // YYYY/MM → YYYY-MM
        // substring + replace 적용
        StringExpression ymExpr = Expressions.stringTemplate(
                "replace(substring({0}, 1, 7), '/', '-')",
                perf.performanceDocumentNo
        );

        // DB에서 BigDecimal SUM → 이후 Java에서 Long 변환
        NumberExpression<BigDecimal> sumExpr =
                Expressions.numberTemplate(BigDecimal.class, "sum({0})", perf.performanceQty);

        List<Tuple> result = queryFactory
                .select(
                        ymExpr.as("month"),
                        sumExpr
                )
                .from(perf)
                .leftJoin(perf.productionPlan, plan)
                .leftJoin(plan.itemLine, itemLine)
                .leftJoin(itemLine.line, line)
                .where(
                        line.factory.factoryCode.eq(factoryCode),
                        ymExpr.in(monthStrings)
                )
                .groupBy(ymExpr)
                .fetch();

        Map<YearMonth, Long> sumByMonth = new HashMap<>();

        for (Tuple tuple : result) {

            String ym = tuple.get(0, String.class);

            // Long 변환 방식
            BigDecimal totalDecimal = tuple.get(1, BigDecimal.class);
            Long total = (totalDecimal != null) ? totalDecimal.longValue() : 0L;

            if (ym != null) {
                sumByMonth.put(YearMonth.parse(ym), total);
            }
        }

        return sumByMonth;
    }
}
