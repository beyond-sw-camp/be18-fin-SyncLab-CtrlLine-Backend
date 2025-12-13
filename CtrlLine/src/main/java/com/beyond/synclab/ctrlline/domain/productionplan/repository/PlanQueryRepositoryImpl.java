package com.beyond.synclab.ctrlline.domain.productionplan.repository;

import com.beyond.synclab.ctrlline.common.util.QuerydslUtils;
import com.beyond.synclab.ctrlline.domain.factory.entity.QFactories;
import com.beyond.synclab.ctrlline.domain.item.entity.QItems;
import com.beyond.synclab.ctrlline.domain.itemline.entity.QItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.QLines;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.QProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.*;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.QProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.QUsers;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class PlanQueryRepositoryImpl implements PlanQueryRepository{

    private final JPAQueryFactory queryFactory;
    private static final String SALES_MANAGER_ALIAS = "salesManager";
    private static final String PROD_MANAGER_ALIAS = "prodManager";

    @Override
    public List<GetProductionPlanScheduleResponseDto> findSchedule(GetProductionPlanScheduleRequestDto requestDto) {
        QProductionPlans plan = QProductionPlans.productionPlans;
        QProductionPerformances perf = QProductionPerformances.productionPerformances;
        QFactories fac = QFactories.factories;
        QLines line = QLines.lines;
        QItemsLines il = QItemsLines.itemsLines;
        QItems item = QItems.items;
        QUsers prodManager = new QUsers(PROD_MANAGER_ALIAS);
        QUsers salesManager = new QUsers(SALES_MANAGER_ALIAS);

        return queryFactory
                .select(
                        Projections.constructor(
                                GetProductionPlanScheduleResponseDto.class,
                                plan.id,

                                line.lineCode,
                                line.lineName,

                                fac.factoryCode,
                                fac.factoryName,

                                salesManager.empNo.as("salesManagerNo"),
                                prodManager.empNo.as("productionManagerNo"),

                                plan.documentNo,

                                item.itemName,
                                item.itemCode,

                                plan.status,
                                plan.dueDate,
                                plan.plannedQty,

                                plan.startTime,
                                plan.endTime,

                                perf.endTime.max().as("actualEndTime"),

                                plan.remark
                        )
                )
                .from(plan)
                .leftJoin(plan.itemLine, il)
                .leftJoin(il.item, item)
                .leftJoin(il.line, line)
                .leftJoin(line.factory, fac)
                .leftJoin(plan.productionManager, prodManager)
                .leftJoin(plan.salesManager, salesManager)
                .leftJoin(perf).on(perf.productionPlan.eq(plan))
                .where(
                        factoryCodeEq(requestDto.factoryCode()),
                        lineCodeEq(requestDto.lineCode()),
                        factoryNameContains(requestDto.factoryName()),
                        lineNameContains(requestDto.lineName()),
                        plan.status.ne(ProductionPlans.PlanStatus.RETURNED),
                        timeOverlap(
                                requestDto.startTime(),
                                requestDto.endTime()
                        )
                )
                .groupBy(
                        plan.id,
                        line.lineCode,
                        line.lineName,
                        fac.factoryCode,
                        fac.factoryName,
                        salesManager.empNo,
                        prodManager.empNo,
                        plan.documentNo,
                        item.itemName,
                        item.itemCode,
                        plan.status,
                        plan.dueDate,
                        plan.plannedQty,
                        plan.startTime,
                        plan.endTime,
                        plan.remark
                )
                .orderBy(plan.startTime.asc())
                .fetch();
    }

    @Override
    public Page<GetProductionPlanListResponseDto> findPlanList(SearchProductionPlanCommand command, Pageable pageable) {
        QProductionPlans plan = QProductionPlans.productionPlans;
        QItemsLines il = QItemsLines.itemsLines;
        QItems item = QItems.items;
        QLines line = QLines.lines;
        QFactories fac = QFactories.factories;
        QUsers salesManager = new QUsers(SALES_MANAGER_ALIAS);
        QUsers prodManager = new QUsers(PROD_MANAGER_ALIAS);

        Map<String, Expression<? extends Comparable<?>>> sortMapping = Map.ofEntries(

                // ===== 기본 식별 / 상태 =====
                Map.entry("planId", plan.id),
                Map.entry("documentNo", plan.documentNo),
                Map.entry("status", plan.status),

                // ===== 날짜 =====
                Map.entry("dueDate", plan.dueDate),
                Map.entry("startTime", plan.startTime),
                Map.entry("endTime", plan.endTime),
                Map.entry("createdAt", plan.createdAt),
                Map.entry("updatedAt", plan.updatedAt),

                // ===== 수량 =====
                Map.entry("plannedQty", plan.plannedQty),

                // ===== 공장 =====
                Map.entry("factoryCode", fac.factoryCode),
                Map.entry("factoryName", fac.factoryName),

                // ===== 라인 =====
                Map.entry("lineCode", line.lineCode),
                Map.entry("lineName", line.lineName),

                // ===== 품목 =====
                Map.entry("itemCode", item.itemCode),
                Map.entry("itemName", item.itemName),

                // ===== 영업 담당자 =====
                Map.entry("salesManagerNo", salesManager.empNo),
                Map.entry("salesManagerName", salesManager.name),

                // ===== 생산 담당자 =====
                Map.entry("productionManagerNo", prodManager.empNo),
                Map.entry("productionManagerName", prodManager.name)
        );

        List<OrderSpecifier<?>> orders =
                QuerydslUtils.getSortOrDefault(
                        pageable.getSort(),
                        sortMapping,
                        plan.createdAt.desc()
                );

        // content query
        List<GetProductionPlanListResponseDto> content =
                queryFactory
                        .select(
                                Projections.fields(
                                        GetProductionPlanListResponseDto.class,
                                        plan.id,
                                        plan.documentNo,
                                        plan.status,
                                        fac.factoryName,
                                        salesManager.name.as("salesManagerName"),
                                        prodManager.name.as("productionManagerName"),
                                        item.itemName,
                                        plan.plannedQty,
                                        plan.dueDate,
                                        plan.remark,
                                        plan.createdAt
                                )
                        )
                        .from(plan)
                        .leftJoin(plan.itemLine, il)
                        .leftJoin(il.item, item)
                        .leftJoin(il.line, line)
                        .leftJoin(line.factory, fac)
                        .leftJoin(plan.salesManager, salesManager)
                        .leftJoin(plan.productionManager, prodManager)
                        .where(findPlanListWhere(command, plan, fac, item, salesManager, prodManager))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .orderBy(orders.toArray(OrderSpecifier[]::new))
                        .fetch();

        // count query
        Long total =
                queryFactory
                        .select(plan.count())
                        .from(plan)
                        .leftJoin(plan.itemLine, il)
                        .leftJoin(il.item, item)
                        .leftJoin(il.line, line)
                        .leftJoin(line.factory, fac)
                        .leftJoin(plan.salesManager, salesManager)
                        .leftJoin(plan.productionManager, prodManager)
                        .where(findPlanListWhere(command, plan, fac, item, salesManager, prodManager))
                        .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Optional<GetProductionPlanDetailResponseDto> findPlanDetail(Long planId) {
        QProductionPlans plan = QProductionPlans.productionPlans;
        QItemsLines il = QItemsLines.itemsLines;
        QItems item = QItems.items;
        QLines line = QLines.lines;
        QFactories fac = QFactories.factories;
        QUsers salesManager = new QUsers(SALES_MANAGER_ALIAS);
        QUsers prodManager = new QUsers(PROD_MANAGER_ALIAS);
        QProductionPerformances perf = QProductionPerformances.productionPerformances;

        return Optional.ofNullable(
                queryFactory
                        .select(Projections.fields(
                                GetProductionPlanDetailResponseDto.class,

                                plan.id,
                                plan.documentNo.as("planDocumentNo"),
                                plan.dueDate,
                                plan.status,

                                salesManager.empNo.as("salesManagerNo"),
                                salesManager.name.as("salesManagerName"),
                                prodManager.empNo.as("productionManagerNo"),
                                prodManager.name.as("productionManagerName"),

                                plan.startTime,
                                plan.endTime,
                                perf.endTime.max().as("actualEndTime"),

                                fac.id.as("factoryId"),
                                fac.factoryCode,
                                fac.factoryName,

                                item.id.as("itemId"),
                                item.itemSpecification,
                                item.itemUnit,
                                item.itemCode,
                                item.itemName,

                                plan.plannedQty,

                                line.id.as("lineId"),
                                line.lineCode,
                                line.lineName,

                                plan.remark
                        ))
                        .from(plan)
                        .leftJoin(plan.itemLine, il)
                        .leftJoin(il.item, item)
                        .leftJoin(il.line, line)
                        .leftJoin(line.factory, fac)
                        .leftJoin(plan.salesManager, salesManager)
                        .leftJoin(plan.productionManager, prodManager)
                        .leftJoin(perf).on(
                                perf.productionPlan.eq(plan)
                                        .and(perf.isDeleted.isFalse())
                        )
                        .where(plan.id.eq(planId))
                        .groupBy(
                                plan.id,
                                plan.documentNo,
                                plan.dueDate,
                                plan.status,
                                salesManager.empNo,
                                salesManager.name,
                                prodManager.empNo,
                                prodManager.name,
                                plan.startTime,
                                plan.endTime,
                                fac.id,
                                fac.factoryCode,
                                fac.factoryName,
                                item.id,
                                item.itemSpecification,
                                item.itemUnit,
                                item.itemCode,
                                item.itemName,
                                plan.plannedQty,
                                line.id,
                                line.lineCode,
                                line.lineName,
                                plan.remark
                        )
                        .fetchOne()
        );
    }

    private BooleanExpression[] findPlanListWhere(
            SearchProductionPlanCommand command,
            QProductionPlans plan,
            QFactories fac,
            QItems item,
            QUsers salesManager,
            QUsers prodManager
    ) {
        return new BooleanExpression[]{
                statusIn(command.status(), plan),
                factoryNameContains(command.factoryName(), fac),
                factoryCodeEq(command.factoryCode(), fac),
                itemNameContains(command.itemName(), item),
                itemCodeEq(command.itemCode(), item),
                salesManagerNameContains(command.salesManagerName(), salesManager),
                salesManagerNoEq(command.salesManagerNo(), salesManager),
                productionManagerNameContains(command.productionManagerName(), prodManager),
                productionManagerNoEq(command.productionManagerNo(), prodManager),
                dueDateFrom(command.dueDateFrom(), plan),
                dueDateTo(command.dueDateTo(), plan),
                startTimeAfter(command.startTime(), plan),
                endTimeBefore(command.endTime(), plan)
        };
    }

    /* =======================
       BooleanExpression helpers
       ======================= */

    private BooleanExpression factoryCodeEq(String value) {
        return hasText(value)
                ? QFactories.factories.factoryCode.eq(value)
                : null;
    }

    private BooleanExpression lineCodeEq(String value) {
        return hasText(value)
                ? QLines.lines.lineCode.eq(value)
                : null;
    }

    private BooleanExpression factoryNameContains(String value) {
        return hasText(value) ? QFactories.factories.factoryName.contains(value) : null;
    }

    private BooleanExpression lineNameContains(String value) {
        return hasText(value) ? QLines.lines.lineName.contains(value) : null;
    }

    /**
     * [start, end] 구간이 겹치는지 판단
     * plan.start <= req.end AND plan.end >= req.start
     */
    private BooleanExpression timeOverlap(
            LocalDateTime start,
            LocalDateTime end
    ) {
        if (start == null || end == null) return null;

        QProductionPlans plan = QProductionPlans.productionPlans;
        return plan.startTime.loe(end)
                .and(plan.endTime.goe(start));
    }

    private BooleanExpression statusIn(
            List<ProductionPlans.PlanStatus> status, QProductionPlans plan
    ) {
        return status == null || status.isEmpty()
                ? null
                : plan.status.in(status);
    }

    private BooleanExpression factoryNameContains(String v, QFactories fac) {
        return hasText(v) ? fac.factoryName.contains(v) : null;
    }

    private BooleanExpression factoryCodeEq(String v, QFactories fac) {
        return hasText(v) ? fac.factoryCode.eq(v) : null;
    }

    private BooleanExpression itemNameContains(String v, QItems item) {
        return hasText(v) ? item.itemName.contains(v) : null;
    }

    private BooleanExpression itemCodeEq(String v, QItems item) {
        return hasText(v) ? item.itemCode.eq(v) : null;
    }

    private BooleanExpression salesManagerNameContains(String v, QUsers u) {
        return hasText(v) ? u.name.contains(v) : null;
    }

    private BooleanExpression salesManagerNoEq(String v, QUsers u) {
        return hasText(v) ? u.empNo.eq(v) : null;
    }

    private BooleanExpression productionManagerNameContains(String v, QUsers u) {
        return hasText(v) ? u.name.contains(v) : null;
    }

    private BooleanExpression productionManagerNoEq(String v, QUsers u) {
        return hasText(v) ? u.empNo.eq(v) : null;
    }

    private BooleanExpression dueDateFrom(LocalDate d, QProductionPlans plan) {
        return d != null ? plan.dueDate.goe(d) : null;
    }

    private BooleanExpression dueDateTo(LocalDate d, QProductionPlans plan) {
        return d != null ? plan.dueDate.loe(d) : null;
    }

    private BooleanExpression startTimeAfter(LocalDateTime t, QProductionPlans plan) {
        return t != null ? plan.startTime.goe(t) : null;
    }

    private BooleanExpression endTimeBefore(LocalDateTime t, QProductionPlans plan) {
        return t != null ? plan.endTime.loe(t) : null;
    }
}
