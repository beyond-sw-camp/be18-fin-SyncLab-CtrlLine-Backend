package com.beyond.synclab.ctrlline.common.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SortDto {

    private String sortBy;
    private String direction;

    // ✅ Sort.Order → SortDto 변환 생성자
    public static SortDto from(Sort.Order order) {
        return SortDto.builder()
            .sortBy(order.getProperty())
            .direction(order.getDirection().name().toLowerCase())
            .build();
    }

    // ✅ Sort → List<SortDto> 변환
    public static List<SortDto> from(Sort sort) {
        return sort.stream()
            .map(SortDto::from)
            .toList();
    }
}
