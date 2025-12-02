package com.beyond.synclab.ctrlline.domain.defective.repository;

import com.beyond.synclab.ctrlline.common.util.QuerydslUtils;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveAllResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveListResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveTypesResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveAllRequestDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveListRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.entity.QEquipments;
import com.beyond.synclab.ctrlline.domain.factory.entity.QFactories;
import com.beyond.synclab.ctrlline.domain.item.entity.QItems;
import com.beyond.synclab.ctrlline.domain.itemline.entity.QItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.QLines;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.QProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.QPlanDefectiveXrefs;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.QPlanDefectives;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.QProductionPlans;
import com.beyond.synclab.ctrlline.domain.telemetry.entity.QDefectives;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
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
                createdAtTo(request.toDate()),
                createdAtFrom(request.fromDate()),
                performanceDocNoContains(request.productionPerformanceDocNo()),
                defectiveDocNoContains(request.defectiveDocNo()),
                itemCodeContains(request.itemCode()),
                itemNameContains(request.itemName()),
                lineNameContains(request.lineName()),
                lineCodeContains(request.lineCode()),
                defectiveQtyEq(request.defectiveQty(), defectiveQtyExpr),
                defectiveRateEq(request.defectiveRate())
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
                createdAtTo(request.toDate()),
                createdAtFrom(request.fromDate()),
                performanceDocNoContains(request.productionPerformanceDocNo()),
                defectiveDocNoContains(request.defectiveDocNo()),
                itemCodeContains(request.itemCode()),
                itemNameContains(request.itemName()),
                lineNameContains(request.lineName()),
                lineCodeContains(request.lineCode()),
                defectiveQtyEq(request.defectiveQty(), defectiveQtyExpr),
                defectiveRateEq(request.defectiveRate())
            );

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    @Override
    public List<GetDefectiveAllResponseDto> findAllDefective(SearchDefectiveAllRequestDto request) {
        QPlanDefectives pd = QPlanDefectives.planDefectives;
        QProductionPlans pp = QProductionPlans.productionPlans;
        QItemsLines il = QItemsLines.itemsLines;
        QItems item = QItems.items;
        QLines line = QLines.lines;
        QFactories fac = QFactories.factories;
        QProductionPerformances perf = QProductionPerformances.productionPerformances;

        NumberExpression<BigDecimal> defectiveQtyExpr =
            perf.totalQty.subtract(perf.performanceQty);

        return queryFactory
            .select(Projections.constructor(
                GetDefectiveAllResponseDto.class,
                pd.id,
                pd.defectiveDocumentNo,
                item.id,
                item.itemCode,
                item.itemName,
                item.itemSpecification,
                line.id,
                line.lineCode,
                line.lineName,
                fac.id,
                fac.factoryCode,
                fac.factoryName,
                pp.productionManager.name,
                pp.productionManager.empNo,
                pp.salesManager.name,
                pp.salesManager.empNo,
                defectiveQtyExpr,
                perf.performanceDefectiveRate,
                perf.performanceDocumentNo,
                pp.dueDate,
                pd.createdAt
            ))
            .from(pd)
            .leftJoin(pd.productionPlan, pp)
            .leftJoin(pp.itemLine, il)
            .leftJoin(il.item, item)
            .leftJoin(il.line, line)
            .leftJoin(line.factory, fac)
            .leftJoin(perf).on(perf.productionPlan.id.eq(pp.id))
            .where(
                createdAtFrom(request.fromDate()),
                createdAtTo(request.toDate()),
                dueDateTo(request.dueDate()),
                factoryCodeEq(request.factoryCode()),
                lineCodeEq(request.lineCode()),
                itemIdEq(request.itemId()),
                prodManagerNoContains(request.productionManagerNo()),
                salesManagerNoContains(request.salesManagerNo()),
                performanceDocNoContains(request.productionPerformanceDocNo())
            )
            .orderBy(pd.createdAt.desc())
            .fetch();
    }

    @Override
    public GetDefectiveTypesResponseDto findDefectiveTypes(String factoryCode) {
        QFactories factory = QFactories.factories;
        QLines line = QLines.lines;
        QEquipments equipment = QEquipments.equipments;
        QDefectives defective = QDefectives.defectives;
        QPlanDefectiveXrefs xref = QPlanDefectiveXrefs.planDefectiveXrefs;

        List<GetDefectiveTypesResponseDto.DefectiveTypesItems> result = queryFactory
            .select(Projections.constructor(
                GetDefectiveTypesResponseDto.DefectiveTypesItems.class,
                defective.defectiveName,
                defective.defectiveCode,
                defective.defectiveType,
                xref.defectiveQty.sumBigDecimal()
            ))
            .from(factory)
            .join(line).on(line.factory.factoryCode.eq(factoryCode).and(line.isActive.isTrue()))
            .join(equipment).on(equipment.lineId.eq(line.id).and(equipment.isActive.isTrue()))
            .join(defective).on(defective.equipmentId.eq(equipment.id))
            .join(xref).on(xref.defectiveId.eq(defective.id))
            .where(factory.factoryCode.eq(factoryCode).and(factory.isActive.isTrue()))
            .groupBy(defective.defectiveCode)
            .fetch();


        return GetDefectiveTypesResponseDto.builder()
            .factoryCode(factoryCode)
            .types(result)
            .build();
    }

    private BooleanExpression defectiveDocNoContains(String defectiveDocNo) {
        return defectiveDocNo == null
            ? null
            : QPlanDefectives.planDefectives.defectiveDocumentNo.contains(defectiveDocNo);
    }

    private BooleanExpression itemCodeContains(String itemCode) {
        return itemCode == null ? null : QItems.items.itemCode.contains(itemCode);
    }

    private BooleanExpression itemNameContains(String itemName) {
        return itemName == null ? null : QItems.items.itemName.contains(itemName);
    }

    private BooleanExpression lineNameContains(String lineName) {
        return lineName == null ? null : QLines.lines.lineName.contains(lineName);
    }

    private BooleanExpression lineCodeContains(String lineCode) {
        return lineCode == null ? null : QLines.lines.lineCode.contains(lineCode);
    }

    private BooleanExpression defectiveQtyEq(BigDecimal qty, NumberExpression<BigDecimal> defectiveExpr) {
        return qty == null ? null : defectiveExpr.eq(qty);
    }

    private BooleanExpression defectiveRateEq(BigDecimal rate) {
        return rate == null ? null : QProductionPerformances.productionPerformances.performanceDefectiveRate.eq(rate);
    }

    private BooleanExpression createdAtFrom(LocalDate fromDate) {
        return fromDate != null
            ? QPlanDefectives.planDefectives.createdAt.goe(fromDate.atStartOfDay())
            : null;
    }

    private BooleanExpression createdAtTo(LocalDate toDate) {
        return toDate != null
            ? QPlanDefectives.planDefectives.createdAt.lt(toDate.atStartOfDay().plusDays(1))
            : null;
    }

    private BooleanExpression dueDateTo(LocalDate dueDate) {
        return dueDate != null
            ? QProductionPlans.productionPlans.dueDate.loe(dueDate)
            : null;
    }

    private BooleanExpression factoryCodeEq(String factoryCode) {
        return (factoryCode != null && !factoryCode.isBlank())
            ? QFactories.factories.factoryCode.eq(factoryCode)
            : null;
    }

    private BooleanExpression lineCodeEq(String lineCode) {
        return (lineCode != null && !lineCode.isBlank())
            ? QLines.lines.lineCode.eq(lineCode)
            : null;
    }

    private BooleanExpression itemIdEq(Long itemId) {
        return itemId != null
            ? QItems.items.id.eq(itemId)
            : null;
    }

    private BooleanExpression prodManagerNoContains(String prodManagerNo) {
        return (prodManagerNo != null && !prodManagerNo.isBlank())
            ? QProductionPlans.productionPlans.productionManager.empNo.containsIgnoreCase(prodManagerNo)
            : null;
    }

    private BooleanExpression salesManagerNoContains(String salesManagerNo) {
        return (salesManagerNo != null && !salesManagerNo.isBlank()) ?
            QProductionPlans.productionPlans.salesManager.empNo.containsIgnoreCase(salesManagerNo) : null;
    }

    private BooleanExpression performanceDocNoContains(String perfDocNo) {
        return (perfDocNo != null && !perfDocNo.isBlank()) ?
            QProductionPerformances.productionPerformances.performanceDocumentNo.containsIgnoreCase(
                perfDocNo
            ) : null;
    }
}
