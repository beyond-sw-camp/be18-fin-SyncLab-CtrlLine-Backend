package com.beyond.synclab.ctrlline.domain.item.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItemActRequestDto {
    private List<Long> itemIds;     // 변경 대상 품목 PK 목록
    private Boolean isActive;
}
