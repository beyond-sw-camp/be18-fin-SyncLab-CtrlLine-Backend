package com.beyond.synclab.ctrlline.common.util;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UtilityClass
@SuppressWarnings("all")
public final class QuerydslUtils {

    public List<OrderSpecifier<?>> getSort(
        Sort sort,
        Map<String, ?> mapping // Path 또는 Expression 모두 허용
    ) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (sort == null || sort.isUnsorted()) {
            return orders;
        }

        sort.forEach(order -> {
            Object value = mapping.get(order.getProperty());
            if (value == null) return;

            Expression<? extends Comparable<?>> expr;

            if (value instanceof Expression<?>) {
                expr = (Expression<? extends Comparable<?>>) value;
            } else if (value instanceof Path<?>) {
                expr = (Path<? extends Comparable<?>>) value;
            } else {
                throw new IllegalArgumentException("Unsupported sort mapping type: " + value.getClass());
            }

            orders.add(new OrderSpecifier<>(
                order.isAscending() ? Order.ASC : Order.DESC,
                expr
            ));
        });

        return orders;
    }

    /**
     * 기본 정렬 보장 (order 비어있을 때)
     */
    public List<OrderSpecifier<?>> getSortOrDefault(
            Sort sort,
            Map<String, ?> mapping,
            OrderSpecifier<?> defaultOrder
    ) {
        List<OrderSpecifier<?>> orders = getSort(sort, mapping);
        return orders.isEmpty() ? List.of(defaultOrder) : orders;
    }
}