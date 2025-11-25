package com.beyond.synclab.ctrlline.domain.productionperformance.repository.query;

import com.beyond.synclab.ctrlline.common.util.QuerydslUtils;
import com.beyond.synclab.ctrlline.domain.factory.entity.QFactories;
import com.beyond.synclab.ctrlline.domain.item.entity.QItems;
import com.beyond.synclab.ctrlline.domain.itemline.entity.QItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.QLines;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.QProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.QProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.QUsers;
import com.beyond.synclab.ctrlline.domain.lot.entity.QLots;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ProductionPerformanceQueryRepositoryImpl implements ProductionPerformanceQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GetProductionPerformanceListResponseDto> searchProductionPerformanceList(
            final SearchProductionPerformanceRequestDto condition,
            final Pageable pageable
    ) {
        QProductionPerformances perf = QProductionPerformances.productionPerformances;
        QProductionPlans plan = QProductionPlans.productionPlans;
        QLines line = QLines.lines;
        QFactories factory = QFactories.factories;
        QItemsLines itemLine = QItemsLines.itemsLines;
        QItems item = QItems.items;
        QUsers salesManager = QUsers.users;
        QUsers prodManager = new QUsers("prodManager");
        QLots lot = QLots.lots;

        // 정렬 매핑
        Map<String, Path<? extends Comparable<?>>> sortMapping = Map.of(
                "performanceDocumentNo", perf.performanceDocumentNo,
                "startTime", perf.startTime,
                "endTime", perf.endTime,
                "totalQty", perf.totalQty,
                "performanceQty", perf.performanceQty,
                "performanceDefectiveRate", perf.performanceDefectiveRate
        );

        List<OrderSpecifier<?>> orders =
                QuerydslUtils.getSort(pageable.getSort(), sortMapping);

        if (orders.isEmpty()) {
            orders.add(perf.performanceDocumentNo.desc());
        }

        // SELECT
        List<GetProductionPerformanceListResponseDto> results = queryFactory
                .select(Projections.constructor(
                        GetProductionPerformanceListResponseDto.class,
                        perf.performanceDocumentNo,
                        salesManager.empNo,
                        prodManager.empNo,
                        factory.factoryCode,
                        line.lineCode,
                        item.itemCode,
                        perf.totalQty,
                        perf.performanceQty,
                        perf.performanceDefectiveRate,
                        perf.remark,
                        perf.isDeleted
                ))
                .from(perf)
                .leftJoin(perf.productionPlan, plan)
                .leftJoin(plan.itemLine, itemLine)
                .leftJoin(itemLine.line, line)
                .leftJoin(itemLine.item, item)
                .leftJoin(factory).on(factory.id.eq(line.factoryId))
                .leftJoin(plan.salesManager, salesManager)
                .leftJoin(plan.productionManager, prodManager)
                .leftJoin(lot).on(lot.productionPlanId.eq(plan.id))
                .where(
                        documentNoContains(condition.getDocumentNo()),
                        planDocumentNoContains(condition.getProductionPlanDocumentNo()),
                        itemCodeContains(condition.getItemCode()),
                        factoryCodeEq(condition.getFactoryCode()),
                        lineCodeEq(condition.getLineCode()),
                        salesManagerNameContains(condition.getSalesManagerName()),
                        producerManagerNameContains(condition.getProducerManagerName()),
                        remarkContains(condition.getRemark()),
                        startDateGoe(condition.getStartDate()),
                        endDateLoe(condition.getEndDate()),
                        dueDateEq(condition.getDueDate()),
                        totalQtyBetween(condition.getMinTotalQty(), condition.getMaxTotalQty()),
                        performanceQtyBetween(condition.getMinPerformanceQty(), condition.getMaxPerformanceQty()),
                        defectRateBetween(condition.getMinDefectRate(), condition.getMaxDefectRate()),
                        isDeletedEq(condition.getIsDeleted()),
                        lotNoContains(condition.getLotNo())
                )
                .orderBy(orders.toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // COUNT QUERY
        JPAQuery<Long> countQuery = queryFactory
                .select(perf.count())
                .from(perf)
                .leftJoin(perf.productionPlan, plan)
                .leftJoin(plan.itemLine, itemLine)
                .leftJoin(itemLine.line, line)
                .leftJoin(itemLine.item, item)
                .leftJoin(factory).on(factory.id.eq(line.factoryId))
                .leftJoin(plan.salesManager, salesManager)
                .leftJoin(plan.productionManager, prodManager)
                .where(
                        documentNoContains(condition.getDocumentNo()),
                        planDocumentNoContains(condition.getProductionPlanDocumentNo()),
                        itemCodeContains(condition.getItemCode()),
                        factoryCodeEq(condition.getFactoryCode()),
                        lineCodeEq(condition.getLineCode()),
                        salesManagerNameContains(condition.getSalesManagerName()),
                        producerManagerNameContains(condition.getProducerManagerName()),
                        remarkContains(condition.getRemark()),
                        startDateGoe(condition.getStartDate()),
                        endDateLoe(condition.getEndDate()),
                        dueDateEq(condition.getDueDate()),
                        totalQtyBetween(condition.getMinTotalQty(), condition.getMaxTotalQty()),
                        performanceQtyBetween(condition.getMinPerformanceQty(), condition.getMaxPerformanceQty()),
                        defectRateBetween(condition.getMinDefectRate(), condition.getMaxDefectRate()),
                        isDeletedEq(condition.getIsDeleted()),
                        lotNoContains(condition.getLotNo())
                );

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    // 검색 조건(BooleanExpression)
    private BooleanExpression documentNoContains(String documentNo) {
        return (documentNo == null || documentNo.isEmpty())
                ? null : QProductionPerformances.productionPerformances.performanceDocumentNo.contains(documentNo);
    }

    private BooleanExpression planDocumentNoContains(String planDocumentNo) {
        return (planDocumentNo == null || planDocumentNo.isEmpty())
                ? null : QProductionPlans.productionPlans.documentNo.contains(planDocumentNo);
    }

    private BooleanExpression itemCodeContains(String itemCode) {
        return (itemCode == null || itemCode.isEmpty())
                ? null : QItems.items.itemCode.contains(itemCode);
    }

    private BooleanExpression factoryCodeEq(String factoryCode) {
        return (factoryCode == null || factoryCode.isEmpty())
                ? null : QFactories.factories.factoryCode.eq(factoryCode);
    }

    private BooleanExpression lineCodeEq(String lineCode) {
        return (lineCode == null || lineCode.isEmpty())
                ? null : QLines.lines.lineCode.eq(lineCode);
    }

    private BooleanExpression salesManagerNameContains(String name) {
        return (name == null || name.isEmpty())
                ? null : QUsers.users.name.contains(name);
    }

    private BooleanExpression producerManagerNameContains(String name) {
        return (name == null || name.isEmpty())
                ? null : new QUsers("prodManager").name.contains(name);
    }

    private BooleanExpression lotNoContains(String lotNo) {
        return (lotNo == null || lotNo.isEmpty())
                ? null : QLots.lots.lotNo.contains(lotNo);
    }

    private BooleanExpression remarkContains(String remark) {
        return (remark == null || remark.isEmpty())
                ? null : QProductionPerformances.productionPerformances.remark.contains(remark);
    }

    private BooleanExpression startDateGoe(String startDate) {
        return (startDate == null || startDate.isEmpty())
                ? null : QProductionPerformances.productionPerformances.startTime.goe(
                java.time.LocalDate.parse(startDate).atStartOfDay()
        );
    }

    private BooleanExpression endDateLoe(String endDate) {
        return (endDate == null || endDate.isEmpty())
                ? null : QProductionPerformances.productionPerformances.endTime.loe(
                java.time.LocalDate.parse(endDate).atTime(23, 59, 59)
        );
    }

    private BooleanExpression dueDateEq(String dueDate) {
        return (dueDate == null || dueDate.isEmpty())
                ? null : QProductionPlans.productionPlans.dueDate.eq(
                java.time.LocalDate.parse(dueDate)
        );
    }

    private BooleanExpression totalQtyBetween(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        if (min != null && max != null)
            return QProductionPerformances.productionPerformances.totalQty.between(min, max);
        if (min != null)
            return QProductionPerformances.productionPerformances.totalQty.goe(min);
        return QProductionPerformances.productionPerformances.totalQty.loe(max);
    }

    private BooleanExpression performanceQtyBetween(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        if (min != null && max != null)
            return QProductionPerformances.productionPerformances.performanceQty.between(min, max);
        if (min != null)
            return QProductionPerformances.productionPerformances.performanceQty.goe(min);
        return QProductionPerformances.productionPerformances.performanceQty.loe(max);
    }

    private BooleanExpression defectRateBetween(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        if (min != null && max != null)
            return QProductionPerformances.productionPerformances.performanceDefectiveRate.between(min, max);
        if (min != null)
            return QProductionPerformances.productionPerformances.performanceDefectiveRate.goe(min);
        return QProductionPerformances.productionPerformances.performanceDefectiveRate.loe(max);
    }

    BooleanExpression isDeletedEq(Boolean isDeleted) {
        return (isDeleted == null) ? null :
                QProductionPerformances.productionPerformances.isDeleted.eq(isDeleted);
    }
}
