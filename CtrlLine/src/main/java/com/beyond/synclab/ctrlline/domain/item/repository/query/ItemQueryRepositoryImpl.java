package com.beyond.synclab.ctrlline.domain.item.repository.query;

import com.beyond.synclab.ctrlline.domain.item.dto.request.SearchItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.QItems;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ItemQueryRepositoryImpl implements ItemQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Items> searchItems(final SearchItemRequestDto keyword, final Pageable pageable) {
        QItems item = QItems.items;

        // 정렬 기준 매핑
        Map<String, Path<? extends Comparable<?>>> sortMapping = Map.of(
                "itemCode", item.itemCode,
                "itemName", item.itemName,
                "itemSpecification", item.itemSpecification,
                "itemStatus", item.itemStatus
        );

        // Pageable 기반 정렬 변환
        List<OrderSpecifier<?>> orders =
                com.beyond.synclab.ctrlline.common.util.QuerydslUtils.getSort(pageable.getSort(), sortMapping);

        // 정렬 기본값: 품목코드 오름차순
        if (orders.isEmpty()) {
            orders.add(item.itemCode.asc());
        }

        // SELECT
        List<Items> results = queryFactory
                .selectFrom(item)
                .where(
                        itemCodeContains(keyword.getItemCode()),
                        itemNameContains(keyword.getItemName()),
                        specificationContains(keyword.getItemSpecification()),
                        statusEq(keyword.getItemStatus()),
                        isActiveEq(keyword.getIsActive())
                )
                .orderBy(orders.toArray(new OrderSpecifier[0])) // 정렬로직
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // COUNT 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(item.count())
                .from(item)
                .where(
                        itemCodeContains(keyword.getItemCode()),
                        itemNameContains(keyword.getItemName()),
                        specificationContains(keyword.getItemSpecification()),
                        statusEq(keyword.getItemStatus()),
                        isActiveEq(keyword.getIsActive())
                );

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    // 품목사용여부 검색
    private BooleanExpression isActiveEq(Boolean isActive) {
        if (isActive == null) return null;       // 전체 조회
        return QItems.items.isActive.eq(isActive); // true or false 필터
    }

    // 품목코드 검색
    private BooleanExpression itemCodeContains(String itemCode) {
        return (itemCode == null || itemCode.isEmpty()) ? null
                : QItems.items.itemCode.contains(itemCode);
    }

    // 품목명 검색
    private BooleanExpression itemNameContains(String name) {
        return (name == null || name.isEmpty()) ? null
                : QItems.items.itemName.contains(name);
    }

    // 규격 검색
    private BooleanExpression specificationContains(String specification) {
        return (specification == null || specification.isEmpty()) ? null
                : QItems.items.itemSpecification.contains(specification);
    }

    // 품목구분 검색
    private BooleanExpression statusEq(com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus status) {
        return (status == null) ? null : QItems.items.itemStatus.eq(status);
    }
}
