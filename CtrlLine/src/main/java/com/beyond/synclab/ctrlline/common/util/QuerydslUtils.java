package com.beyond.synclab.ctrlline.common.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public final class QuerydslUtils {

    // 인스턴스화 방지 (유틸 클래스)
    private QuerydslUtils() {
    }

    // Pageable Sort → QueryDSL OrderSpecifier 변환 메서드
    public static <T extends Comparable<? super T>>
    List<OrderSpecifier<?>> getSort(
            Sort sort,
            Map<String, ? extends Path<? extends Comparable<?>>> mapping
    ) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (sort == null || sort.isUnsorted()) {
            return orders;
        }

        sort.forEach(order -> {
            Path<? extends Comparable<?>> path = mapping.get(order.getProperty());
            if (path != null) {
                orders.add(new OrderSpecifier<>(
                        order.isAscending() ? Order.ASC : Order.DESC,
                        path
                ));
            }
        });
        return orders;
    }
}