package com.beyond.synclab.ctrlline.domain.item.dto.response;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
