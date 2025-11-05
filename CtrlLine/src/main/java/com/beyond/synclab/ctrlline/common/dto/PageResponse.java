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
public class PageResponse<T> {
    private final List<T> content;       // 실제 데이터 목록
    private final PageInfoDto pageInfo;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .pageInfo(PageInfoDto.from(page))
            .build();
    }
}
