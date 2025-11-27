package com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.all;

import com.beyond.synclab.ctrlline.domain.factory.entity.QFactories;
import com.beyond.synclab.ctrlline.domain.item.entity.QItems;
import com.beyond.synclab.ctrlline.domain.itemline.entity.QItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.QLines;
import com.beyond.synclab.ctrlline.domain.lot.entity.QLots;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchAllProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetAllProductionPerformanceResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.QProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.QProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.QUsers;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductionPerformanceAllQueryRepositoryImpl
        implements ProductionPerformanceAllQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<GetAllProductionPerformanceResponseDto> searchAll(
            final SearchAllProductionPerformanceRequestDto condition
    ) {
        log.debug(condition.toString());

        QProductionPerformances perf = QProductionPerformances.productionPerformances;
        QProductionPlans plan = QProductionPlans.productionPlans;
        QItemsLines itemLine = QItemsLines.itemsLines;
        QItems item = QItems.items;
        QLines line = QLines.lines;
        QFactories factory = QFactories.factories;
        QUsers salesManager = QUsers.users;
        QUsers prodManager = new QUsers("prodManager");
        QLots lot = QLots.lots;

        NumberExpression<BigDecimal> defectiveQtyExpr =
                perf.totalQty.subtract(perf.performanceQty);

        return queryFactory
                .select(Projections.constructor(
                        GetAllProductionPerformanceResponseDto.class,
                        perf.id,
                        perf.performanceDocumentNo,
                        factory.factoryCode,
                        factory.factoryName,
                        line.lineCode,
                        line.lineName,
                        salesManager.empNo,
                        salesManager.name,
                        prodManager.empNo,
                        prodManager.name,
                        lot.lotNo,
                        item.itemCode,
                        item.itemName,
                        item.itemSpecification,
                        item.itemUnit,
                        perf.totalQty,
                        perf.performanceQty,
                        defectiveQtyExpr,
                        perf.performanceDefectiveRate,
                        perf.startTime,
                        perf.endTime,
                        plan.dueDate,
                        perf.remark,
                        perf.createdAt,
                        perf.updatedAt
                ))
                .from(perf)
                .leftJoin(perf.productionPlan, plan)
                .leftJoin(plan.itemLine, itemLine)
                .leftJoin(itemLine.item, item)
                .leftJoin(itemLine.line, line)
                .leftJoin(line.factory, factory)
                .leftJoin(plan.salesManager, salesManager)
                .leftJoin(plan.productionManager, prodManager)
                .leftJoin(lot).on(lot.productionPlanId.eq(plan.id))
                .where(
                        documentNoBetween(condition.getDocumentNoStart(), condition.getDocumentNoEnd()),
                        stringEq(factory.factoryCode, condition.getFactoryCode()),
                        stringEq(line.lineCode, condition.getLineCode()),
                        stringEq(salesManager.empNo, condition.getSalesManagerEmpNo()),
                        stringEq(prodManager.empNo, condition.getProductionManagerEmpNo()),
                        contains(lot.lotNo, condition.getLotNo()),
                        contains(item.itemCode, condition.getItemCode()),
                        contains(item.itemName, condition.getItemName()),
                        contains(item.itemSpecification, condition.getSpecification()),
                        contains(item.itemUnit, condition.getUnit()),
                        dateTimeBetween(perf.startTime, condition.getStartDateTimeStart(), condition.getStartDateTimeEnd()),
                        dateTimeBetween(perf.endTime, condition.getEndDateTimeStart(), condition.getEndDateTimeEnd()),
                        dateBetween(plan.dueDate, condition.getDueDateStart(), condition.getDueDateEnd()),
                        numberBetween(perf.totalQty, condition.getMinTotalQty(), condition.getMaxTotalQty()),
                        numberBetween(perf.performanceQty, condition.getMinPerformanceQty(), condition.getMaxPerformanceQty()),
                        numberBetween(perf.performanceDefectiveRate, condition.getMinDefectiveRate(), condition.getMaxDefectiveRate()),
                        boolEq(perf.isDeleted, condition.getIsDeleted())
                )
                .orderBy(perf.performanceDocumentNo.desc())
                .fetch();
    }

    // --- 공통 조건식 ---

    private BooleanExpression stringEq(StringExpression column, String value) {
        return (value == null || value.isBlank()) ? null : column.eq(value);
    }

    private BooleanExpression contains(StringExpression column, String value) {
        return (value == null || value.isBlank()) ? null : column.contains(value);
    }

    private BooleanExpression boolEq(BooleanPath column, Boolean value) {
        return (value == null) ? null : column.eq(value);
    }

    private BooleanExpression numberBetween(NumberPath<BigDecimal> column,
                                            BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        if (min != null && max != null) return column.between(min, max);
        if (min != null) return column.goe(min);
        return column.loe(max);
    }

    private BooleanExpression dateBetween(DatePath<LocalDate> column,
                                          String from, String to) {
        LocalDate fromDt = parseDate(from);
        LocalDate toDt = parseDate(to);

        if (fromDt == null && toDt == null) return null;
        if (fromDt != null && toDt != null) return column.between(fromDt, toDt);
        if (fromDt != null) return column.goe(fromDt);
        return column.loe(toDt);
    }

    private BooleanExpression dateTimeBetween(DateTimePath<LocalDateTime> column,
                                              String from, String to) {
        LocalDateTime fromDt = parseDateTime(from);
        LocalDateTime toDt = parseDateTime(to);

        if (fromDt == null && toDt == null) return null;
        if (fromDt != null && toDt != null) return column.between(fromDt, toDt);
        if (fromDt != null) return column.goe(fromDt);
        return column.loe(toDt);
    }

    // --- 전표번호 substring(1, 10) 추출 (CtrlLine 스타일) ---
    private StringExpression extractDocDate() {
        QProductionPerformances perf = QProductionPerformances.productionPerformances;
        return Expressions.stringTemplate(
                "substring({0}, 1, 10)",
                perf.performanceDocumentNo
        );
    }

    private BooleanExpression documentNoBetween(String from, String to) {
        if ((from == null || from.isBlank()) && (to == null || to.isBlank())) {
            return null;
        }

        StringExpression docDate = extractDocDate();

        if (!isBlank(from) && !isBlank(to)) {
            return docDate.between(from, to);
        }
        if (!isBlank(from)) {
            return docDate.goe(from);
        }
        return docDate.loe(to);
    }

    private boolean isBlank(String val) {
        return val == null || val.isBlank();
    }

    private LocalDate parseDate(String date) {
        return (date == null || date.isEmpty())
                ? null
                : LocalDate.parse(date);
    }

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
}
