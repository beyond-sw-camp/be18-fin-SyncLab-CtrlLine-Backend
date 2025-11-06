package com.beyond.synclab.ctrlline.common.dto;


import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageInfoDto {
    private final int currentPage;
    private final int pageSize;
    private final int totalPages;
    private final long totalElements;
    private final List<SortDto> sort;

    public static <T> PageInfoDto from(Page<T> page) {
        return PageInfoDto.builder()
            .currentPage(page.getNumber() + 1) // 0-based → 1-based
            .pageSize(page.getSize())
            .totalPages(page.getTotalPages())
            .totalElements(page.getTotalElements())
            .sort(SortDto.from(page.getSort()))
            .build();
    }
}
