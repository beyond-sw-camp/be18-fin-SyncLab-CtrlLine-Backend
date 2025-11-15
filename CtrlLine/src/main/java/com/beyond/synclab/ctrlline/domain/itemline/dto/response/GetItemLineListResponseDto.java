package com.beyond.synclab.ctrlline.domain.itemline.dto.response;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 조회 탭 - 라인별 생산 가능 품목 조회용 DTO
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetItemLineListResponseDto {

    private final String itemCode;
    private final String itemName;
    private final String itemSpecification;

    public static GetItemLineListResponseDto fromEntity(Items item) {
        return GetItemLineListResponseDto.builder()
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .itemSpecification(item.getItemSpecification())
                .build();
    }
}
