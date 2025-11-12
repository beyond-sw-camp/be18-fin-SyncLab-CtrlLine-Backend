package com.beyond.synclab.ctrlline.domain.item.dto.response;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 품목 목록 조회용 DTO
 * - 사용자에게 PK는 숨기고 Code 기반으로 노출
 * - 리스트 테이블에서 한눈에 식별 가능한 최소 필드만 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetItemListResponseDto {
    private Long id;
    private String itemCode;
    private String itemName;
    private String itemSpecification;
    private String itemUnit;
    private ItemStatus itemStatus;
    private Boolean isActive;

    public static GetItemListResponseDto fromEntity(Items item) {
        return GetItemListResponseDto.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .itemSpecification(item.getItemSpecification())
                .itemUnit(item.getItemUnit())
                .itemStatus(item.getItemStatus())
                .isActive(item.getIsActive())
                .build();
    }
}
