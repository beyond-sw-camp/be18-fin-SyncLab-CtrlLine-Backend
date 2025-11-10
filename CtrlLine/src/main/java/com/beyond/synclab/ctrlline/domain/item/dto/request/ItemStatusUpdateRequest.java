package com.beyond.synclab.ctrlline.domain.item.dto.request;

import lombok.Getter;

import java.util.List;

/**
 * 품목 상태(활성/비활성) 변경 요청 DTO
 * 단건/다건 통합 처리용
 */
@Getter
public class ItemStatusUpdateRequest {
    private List<Long> itemIds;  // 변경 대상 품목 ID 리스트
    private Boolean isActive;    // true = 활성화, false = 비활성화
}
