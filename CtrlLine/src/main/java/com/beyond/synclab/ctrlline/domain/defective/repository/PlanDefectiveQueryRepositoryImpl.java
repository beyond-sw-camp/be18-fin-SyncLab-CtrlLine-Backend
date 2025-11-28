package com.beyond.synclab.ctrlline.domain.defective.repository;

import com.beyond.synclab.ctrlline.common.util.QuerydslUtils;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveListResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveListRequestDto;
import com.beyond.synclab.ctrlline.domain.item.entity.QItems;
import com.beyond.synclab.ctrlline.domain.itemline.entity.QItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.QLines;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.QProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.QPlanDefectives;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.QProductionPlans;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class PlanDefectiveQueryRepositoryImpl implements PlanDefectiveQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GetDefectiveListResponseDto> findDefectiveList(
        SearchDefectiveListRequestDto request, Pageable pageable) {
        QPlanDefectives pd = QPlanDefectives.planDefectives;
        QProductionPlans pp = QProductionPlans.productionPlans;
        QItemsLines il = QItemsLines.itemsLines;
        QItems item = QItems.items;
        QLines line = QLines.lines;
        QProductionPerformances perf = QProductionPerformances.productionPerformances;

        NumberExpression<BigDecimal> defectiveQtyExpr =
            perf.totalQty.subtract(perf.performanceQty);

        Map<String, Object> sortMapping = Map.of(
            "performanceDocumentNo", perf.performanceDocumentNo,
            "createdAt", pd.createdAt,
            "defectiveQty", defectiveQtyExpr,
            "performanceDefectiveRate", perf.performanceDefectiveRate
        );

        List<OrderSpecifier<?>> orders =
            QuerydslUtils.getSort(pageable.getSort(), sortMapping);

        if (orders.isEmpty()) {
            orders.add(pd.createdAt.desc());
        }

        List<GetDefectiveListResponseDto> contents = queryFactory
            .select(Projections.constructor(
                GetDefectiveListResponseDto.class,
                pd.id,
                pd.defectiveDocumentNo,
                item.id,
                item.itemCode,
                item.itemName,
                line.id,
                line.lineCode,
                line.lineName,
                defectiveQtyExpr,
                perf.performanceDefectiveRate,
                perf.performanceDocumentNo,
                pd.createdAt
            ))
            .from(pd)
            .leftJoin(pd.productionPlan, pp)
            .leftJoin(pp.itemLine, il)
            .leftJoin(il.item, item)
            .leftJoin(il.line, line)
            .leftJoin(perf).on(perf.productionPlan.id.eq(pp.id))
            .where(
                createdAtTo(request),
                createdAtFrom(request),
                performanceDocNoContains(request)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(orders.toArray(OrderSpecifier[]::new))
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(pd.count())
            .from(pd)
            .leftJoin(pd.productionPlan, pp)
            .leftJoin(perf).on(perf.productionPlan.id.eq(pp.id))
            .where(
                createdAtTo(request),
                createdAtFrom(request),
                performanceDocNoContains(request)
            );

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    private BooleanExpression createdAtFrom(SearchDefectiveListRequestDto req) {
        return req.fromDate() != null ?
            QPlanDefectives.planDefectives.createdAt.goe(req.fromDate().atStartOfDay()) : null;
    }

    private BooleanExpression createdAtTo(SearchDefectiveListRequestDto req) {
        return req.toDate() != null ?
            QPlanDefectives.planDefectives.createdAt.loe(req.toDate().atStartOfDay().plusDays(1)) : null;
    }

    private BooleanExpression performanceDocNoContains(SearchDefectiveListRequestDto req) {
        return (req.productionPerformanceDocNo() != null && !req.productionPerformanceDocNo().isBlank()) ?
            QProductionPerformances.productionPerformances.performanceDocumentNo.containsIgnoreCase(
                req.productionPerformanceDocNo()
            ) : null;
    }
}
