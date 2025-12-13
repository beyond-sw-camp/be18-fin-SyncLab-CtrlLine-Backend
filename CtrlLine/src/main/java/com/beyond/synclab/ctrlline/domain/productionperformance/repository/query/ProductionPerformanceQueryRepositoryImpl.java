package com.beyond.synclab.ctrlline.domain.productionperformance.repository.query;

import com.beyond.synclab.ctrlline.common.util.QuerydslUtils;
import com.beyond.synclab.ctrlline.domain.factory.entity.QFactories;
import com.beyond.synclab.ctrlline.domain.item.entity.QItems;
import com.beyond.synclab.ctrlline.domain.itemline.entity.QItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.QLines;
import com.beyond.synclab.ctrlline.domain.lot.entity.QLots;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.QProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.QProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.QUsers;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ProductionPerformanceQueryRepositoryImpl implements ProductionPerformanceQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final String PROD_MANAGER_ALIAS = "prodManager";

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
        QUsers prodManager = new QUsers(PROD_MANAGER_ALIAS);
        QLots lot = QLots.lots;

        // 정렬 매핑
        Map<String, Path<? extends Comparable<?>>> sortMapping = Map.of(
                "performanceDocumentNo", perf.performanceDocumentNo,
                "startTime", perf.startTime,
                "endTime", perf.endTime,
                "totalQty", perf.totalQty,
                "performanceQty", perf.performanceQty,
                "performanceDefectiveRate", perf.performanceDefectiveRate,
                "factoryName", factory.factoryName,
                "lineName", line.lineName,
                "itemName", item.itemName,
                "createdAt",perf.createdAt
        );

        List<OrderSpecifier<?>> orders =
                QuerydslUtils.getSort(pageable.getSort(), sortMapping);

        if (orders.isEmpty()) {
            orders.add(perf.performanceDocumentNo.desc());
        }

        NumberExpression<BigDecimal> defectiveQtyExpr =
                perf.totalQty.subtract(perf.performanceQty);
        // SELECT
        List<GetProductionPerformanceListResponseDto> results = queryFactory
                .select(Projections.fields(
                        GetProductionPerformanceListResponseDto.class,
                        perf.id,
                        perf.performanceDocumentNo,
                        salesManager.empNo,
                        salesManager.name,
                        prodManager.empNo,
                        prodManager.name,
                        factory.factoryCode,
                        factory.factoryName,
                        line.lineCode,
                        line.lineName,
                        item.itemCode,
                        item.itemName,
                        item.itemUnit,
                        perf.totalQty,
                        perf.performanceQty,
                        defectiveQtyExpr,
                        perf.performanceDefectiveRate,
                        perf.remark,
                        perf.isDeleted,
                        perf.createdAt
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
                        documentDateBetween(condition.getDocumentDateFrom(), condition.getDocumentDateTo()),
                        planDocumentNoContains(condition.getProductionPlanDocumentNo()),
                        itemCodeContains(condition.getItemCode()),
                        itemNameContains(condition.getItemName()),
                        factoryCodeEq(condition.getFactoryCode()),
                        factoryNameContains(condition.getFactoryName()),
                        lineCodeEq(condition.getLineCode()),
                        lineNameContains(condition.getLineName()),
                        salesManagerEmpNoEq(condition.getSalesManagerNo()),
                        salesManagerNameContains(condition.getSalesManagerName()),
                        producerManagerEmpNoEq(condition.getProducerManagerNo()),
                        producerManagerNameContains(condition.getProducerManagerName()),
                        remarkContains(condition.getRemark()),
                        startTimeBetween(condition.getStartTimeFrom(), condition.getStartTimeTo()),
                        endTimeBetween(condition.getEndTimeFrom(), condition.getEndTimeTo()),
                        dueDateBetween(condition.getDueDateFrom(), condition.getDueDateTo()),
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
                        documentDateBetween(condition.getDocumentDateFrom(), condition.getDocumentDateTo()),
                        planDocumentNoContains(condition.getProductionPlanDocumentNo()),
                        itemCodeContains(condition.getItemCode()),
                        itemNameContains(condition.getItemName()),
                        factoryCodeEq(condition.getFactoryCode()),
                        factoryNameContains(condition.getFactoryName()),
                        lineCodeEq(condition.getLineCode()),
                        lineNameContains(condition.getLineName()),
                        salesManagerEmpNoEq(condition.getSalesManagerNo()),
                        salesManagerNameContains(condition.getSalesManagerName()),
                        producerManagerEmpNoEq(condition.getProducerManagerNo()),
                        producerManagerNameContains(condition.getProducerManagerName()),
                        remarkContains(condition.getRemark()),
                        startTimeBetween(condition.getStartTimeFrom(), condition.getStartTimeTo()),
                        endTimeBetween(condition.getEndTimeFrom(), condition.getEndTimeTo()),
                        dueDateBetween(condition.getDueDateFrom(), condition.getDueDateTo()),
                        totalQtyBetween(condition.getMinTotalQty(), condition.getMaxTotalQty()),
                        performanceQtyBetween(condition.getMinPerformanceQty(), condition.getMaxPerformanceQty()),
                        defectRateBetween(condition.getMinDefectRate(), condition.getMaxDefectRate()),
                        isDeletedEq(condition.getIsDeleted()),
                        lotNoContains(condition.getLotNo())
                );

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    // 검색 조건(BooleanExpression)
    private BooleanExpression documentDateBetween(String from, String to) {
        if ((from == null || from.isEmpty()) && (to == null || to.isEmpty())) {
            return null;
        }

        // substring(performance_document_no, 1, 10) AS 비교 대상 날짜
        StringExpression docDate = Expressions.stringTemplate(
                "substring({0}, 1, 10)",
                QProductionPerformances.productionPerformances.performanceDocumentNo
        );

        BooleanExpression condition = null;

        if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {
            condition = docDate.between(from, to);
        } else if (from != null && !from.isEmpty()) {
            condition = docDate.goe(from);
        } else if (to != null && !to.isEmpty()) {
            condition = docDate.loe(to);
        }

        return condition;
    }

    private BooleanExpression planDocumentNoContains(String planDocumentNo) {
        return (planDocumentNo == null || planDocumentNo.isEmpty())
                ? null : QProductionPlans.productionPlans.documentNo.contains(planDocumentNo);
    }

    private BooleanExpression itemCodeContains(String itemCode) {
        return (itemCode == null || itemCode.isEmpty())
                ? null : QItems.items.itemCode.contains(itemCode);
    }

    private BooleanExpression itemNameContains(String itemName) {
        return (itemName == null || itemName.isEmpty())
                ? null : QItems.items.itemName.contains(itemName);
    }

    private BooleanExpression factoryCodeEq(String factoryCode) {
        return (factoryCode == null || factoryCode.isEmpty())
                ? null : QFactories.factories.factoryCode.eq(factoryCode);
    }

    private BooleanExpression factoryNameContains(String factoryName) {
        return (factoryName == null || factoryName.isEmpty())
                ? null : QFactories.factories.factoryName.contains(factoryName);
    }

    private BooleanExpression lineCodeEq(String lineCode) {
        return (lineCode == null || lineCode.isEmpty())
                ? null : QLines.lines.lineCode.eq(lineCode);
    }

    private BooleanExpression lineNameContains(String lineName) {
        return (lineName == null || lineName.isEmpty())
                ? null : QLines.lines.lineName.contains(lineName);
    }

    private BooleanExpression salesManagerEmpNoEq(String empNo) {
        return (empNo == null || empNo.isEmpty())
                ? null : QUsers.users.empNo.eq(empNo);
    }

    private BooleanExpression salesManagerNameContains(String name) {
        return (name == null || name.isEmpty())
                ? null : QUsers.users.name.contains(name);
    }

    private BooleanExpression producerManagerEmpNoEq(String empNo) {
        return (empNo == null || empNo.isEmpty())
                ? null : new QUsers(PROD_MANAGER_ALIAS).empNo.eq(empNo);
    }

    private BooleanExpression producerManagerNameContains(String name) {
        QUsers prod = new QUsers(PROD_MANAGER_ALIAS);
        return (name == null || name.isEmpty())
                ? null : prod.name.contains(name);
    }

    private BooleanExpression lotNoContains(String lotNo) {
        return (lotNo == null || lotNo.isEmpty())
                ? null : QLots.lots.lotNo.contains(lotNo);
    }

    private BooleanExpression remarkContains(String remark) {
        return (remark == null || remark.isEmpty())
                ? null : QProductionPerformances.productionPerformances.remark.contains(remark);
    }

    private BooleanExpression startTimeBetween(String from, String to) {
        LocalDateTime fromDt = parseDateTime(from);
        LocalDateTime toDt = parseDateTime(to);

        if (fromDt == null && toDt == null) return null;

        if (fromDt != null && toDt != null)
            return QProductionPerformances.productionPerformances.startTime.between(fromDt, toDt);

        if (fromDt != null)
            return QProductionPerformances.productionPerformances.startTime.goe(fromDt);

        return QProductionPerformances.productionPerformances.startTime.loe(toDt);
    }

    private BooleanExpression endTimeBetween(String from, String to) {
        LocalDateTime fromDt = parseDateTime(from);
        LocalDateTime toDt = parseDateTime(to);

        if (fromDt == null && toDt == null) return null;

        if (fromDt != null && toDt != null)
            return QProductionPerformances.productionPerformances.endTime.between(fromDt, toDt);

        if (fromDt != null)
            return QProductionPerformances.productionPerformances.endTime.goe(fromDt);

        return QProductionPerformances.productionPerformances.endTime.loe(toDt);
    }

    private BooleanExpression dueDateBetween(String from, String to) {
        LocalDate fromDt = parseDate(from);
        LocalDate toDt = parseDate(to);

        if (fromDt == null && toDt == null) return null;

        if (fromDt != null && toDt != null)
            return QProductionPlans.productionPlans.dueDate.between(fromDt, toDt);

        if (fromDt != null)
            return QProductionPlans.productionPlans.dueDate.goe(fromDt);

        return QProductionPlans.productionPlans.dueDate.loe(toDt);
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

    // --- 날짜 변환 유틸 ---
    private LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }

        // LocalDateTime 형태 시도
        try {
            return LocalDateTime.parse(dateTime);
        } catch (Exception ex) {
            // LocalDateTime 포맷이 아닌 경우 → LocalDate 포맷으로 재시도
        }

        // LocalDate 형태 시도
        try {
            LocalDate date = LocalDate.parse(dateTime);
            return date.atStartOfDay();
        } catch (Exception ex) {
            // LocalDate 포맷이 아닌 경우 → 최종 예외 처리로 위임
        }

        // 두 포맷 모두 실패한 경우
        throw new IllegalArgumentException("Invalid datetime format: " + dateTime);
    }

    private LocalDate parseDate(String date) {
        return (date == null || date.isEmpty())
                ? null
                : LocalDate.parse(date);
    }
}
