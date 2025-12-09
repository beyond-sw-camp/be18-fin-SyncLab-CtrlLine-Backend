package com.beyond.synclab.ctrlline.domain.lot.repository.query;

import com.beyond.synclab.ctrlline.common.util.QuerydslUtils;
import com.beyond.synclab.ctrlline.domain.factory.entity.QFactories;
import com.beyond.synclab.ctrlline.domain.item.entity.QItems;
import com.beyond.synclab.ctrlline.domain.itemline.entity.QItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.QLines;
import com.beyond.synclab.ctrlline.domain.lot.dto.request.SearchLotRequestDto;
import com.beyond.synclab.ctrlline.domain.lot.dto.response.GetLotListResponseDto;
import com.beyond.synclab.ctrlline.domain.lot.entity.QLots;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.QProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.QProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.QUsers;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
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
public class LotQueryRepositoryImpl implements LotQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GetLotListResponseDto> searchLotList(
            SearchLotRequestDto condition,
            Pageable pageable
    ) {

        QLots lot = QLots.lots;
        QItems item = QItems.items;
        QItemsLines itemLine = QItemsLines.itemsLines;
        QLines line = QLines.lines;
        QFactories factory = QFactories.factories;
        QUsers prodManager = new QUsers("prodManager");
        QProductionPlans plan = QProductionPlans.productionPlans;
        QProductionPerformances perf = QProductionPerformances.productionPerformances;

        // 정렬 매핑 (생산실적 방식 동일)
        Map<String, Path<? extends Comparable<?>>> sortMapping = Map.of(
                "lotNo", lot.lotNo,
                "createdAt", lot.createdAt,
                "updatedAt", lot.updatedAt
        );

        List<OrderSpecifier<?>> orders =
                QuerydslUtils.getSort(pageable.getSort(), sortMapping);

        if (orders.isEmpty()) {
            orders.add(lot.lotNo.desc());
        }

        // 불량 수량
        NumberExpression<BigDecimal> defectiveQtyExpr =
                perf.totalQty.subtract(perf.performanceQty);

        // 불량률
        NumberExpression<BigDecimal> defectiveRateExpr =
                Expressions.numberTemplate(
                        BigDecimal.class,
                        "CASE WHEN {1} = 0 THEN 0 ELSE (({0}-{1})/{1})*100 END",
                        perf.totalQty, perf.performanceQty
                );

        // SELECT
        List<GetLotListResponseDto> results = queryFactory
                .select(Projections.constructor(
                        GetLotListResponseDto.class,
                        lot.id,
                        lot.lotNo,
                        item.itemCode,
                        item.itemName,
                        perf.performanceQty,
                        defectiveQtyExpr,
                        defectiveRateExpr,
                        lot.createdAt,
                        lot.updatedAt,
                        lot.isDeleted
                ))
                .from(lot)
                .leftJoin(item).on(item.id.eq(lot.itemId))
                .leftJoin(plan).on(plan.id.eq(lot.productionPlanId))
                .leftJoin(itemLine).on(itemLine.id.eq(plan.itemLineId))
                .leftJoin(line).on(line.id.eq(itemLine.lineId))
                .leftJoin(factory).on(factory.id.eq(line.factoryId))
                .leftJoin(perf).on(perf.productionPlanId.eq(plan.id))
                .leftJoin(prodManager).on(prodManager.id.eq(plan.productionManagerId))
                .where(
                        lotNoContains(condition.getLotNo()),
                        itemCodeContains(condition.getItemCode()),
                        itemNameContains(condition.getItemName()),
                        factoryCodeEq(condition.getFactoryCode()),
                        factoryNameContains(condition.getFactoryName()),
                        lineCodeEq(condition.getLineCode()),
                        lineNameContains(condition.getLineName()),
                        productionManagerNoEq(condition.getProductionManagerNo()),
                        productionManagerNameContains(condition.getProductionManagerName()),
                        isDeletedEq(condition.getIsDeleted()),
                        performanceDocumentNoContains(condition.getPerformanceDocumentNo()),
                        defectiveDocumentNoContains(condition.getDefectiveDocumentNo()),
                        createdAtBetween(condition.getCreatedAtFrom(), condition.getCreatedAtTo()),
                        updatedAtBetween(condition.getUpdatedAtFrom(), condition.getUpdatedAtTo())
                )
                .orderBy(orders.toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // COUNT QUERY (생산실적 방식 동일)
        JPAQuery<Long> countQuery = queryFactory
                .select(lot.count())
                .from(lot)
                .leftJoin(item).on(item.id.eq(lot.itemId))
                .leftJoin(plan).on(plan.id.eq(lot.productionPlanId))
                .leftJoin(itemLine).on(itemLine.id.eq(plan.itemLineId))
                .leftJoin(line).on(line.id.eq(itemLine.lineId))
                .leftJoin(factory).on(factory.id.eq(line.factoryId))
                .leftJoin(perf).on(perf.productionPlanId.eq(plan.id))
                .leftJoin(prodManager).on(prodManager.id.eq(plan.productionManagerId))
                .where(
                        lotNoContains(condition.getLotNo()),
                        itemCodeContains(condition.getItemCode()),
                        itemNameContains(condition.getItemName()),
                        factoryCodeEq(condition.getFactoryCode()),
                        factoryNameContains(condition.getFactoryName()),
                        lineCodeEq(condition.getLineCode()),
                        lineNameContains(condition.getLineName()),
                        productionManagerNoEq(condition.getProductionManagerNo()),
                        productionManagerNameContains(condition.getProductionManagerName()),
                        isDeletedEq(condition.getIsDeleted()),
                        performanceDocumentNoContains(condition.getPerformanceDocumentNo()),
                        defectiveDocumentNoContains(condition.getDefectiveDocumentNo()),
                        createdAtBetween(condition.getCreatedAtFrom(), condition.getCreatedAtTo()),
                        updatedAtBetween(condition.getUpdatedAtFrom(), condition.getUpdatedAtTo())
                );

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    // ========== WHERE 조건 ==========
    private BooleanExpression lotNoContains(String lotNo) {
        return (lotNo == null || lotNo.isEmpty()) ? null : QLots.lots.lotNo.contains(lotNo);
    }

    private BooleanExpression itemCodeContains(String itemCode) {
        return (itemCode == null || itemCode.isEmpty()) ? null : QItems.items.itemCode.contains(itemCode);
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

    private BooleanExpression productionManagerNoEq(String managerNo) {
        return (managerNo == null || managerNo.isEmpty())
                ? null : QUsers.users.empNo.eq(managerNo);
    }

    private BooleanExpression productionManagerNameContains(String name) {
        return (name == null || name.isEmpty())
                ? null
                : new QUsers("prodManager").name.contains(name);
    }

    private BooleanExpression isDeletedEq(Boolean isDeleted) {
        return isDeleted == null ? null : QLots.lots.isDeleted.eq(isDeleted);
    }

    private BooleanExpression createdAtBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;

        DateTimePath<LocalDateTime> field = QLots.lots.createdAt;

        if (from != null && to != null)
            return field.between(from.atStartOfDay(), to.atTime(23, 59, 59));

        if (from != null)
            return field.goe(from.atStartOfDay());

        return field.loe(to.atTime(23, 59, 59));
    }

    private BooleanExpression updatedAtBetween(LocalDate from, LocalDate to) {

        if (from == null && to == null) {
            return null;
        }

        DateTimePath<LocalDateTime> field = QLots.lots.updatedAt;

        if (from != null && to != null) {
            return field.between(
                    from.atStartOfDay(),
                    to.atTime(23, 59, 59)
            );
        }

        if (from != null) {
            return field.goe(from.atStartOfDay());
        }

        return field.loe(to.atTime(23, 59, 59));
    }

    private BooleanExpression performanceDocumentNoContains(String docNo) {
        return (docNo == null || docNo.isBlank())
                ? null
                : QProductionPerformances.productionPerformances.performanceDocumentNo.contains(docNo);
    }

    private BooleanExpression defectiveDocumentNoContains(String defectiveDocumentNo) {
        return (defectiveDocumentNo == null || defectiveDocumentNo.isEmpty())
                ? null
                : QProductionPerformances.productionPerformances.performanceDocumentNo.contains(defectiveDocumentNo);
    }
}
