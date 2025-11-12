package com.beyond.synclab.ctrlline.domain.item.dto.response;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetItemListResponseDto {
    private final Long id;
    private final String itemCode;
    private final String itemName;
    private final String itemSpecification;
    private final String itemUnit;
    private final ItemStatus itemStatus;
    private final Boolean isActive;

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
