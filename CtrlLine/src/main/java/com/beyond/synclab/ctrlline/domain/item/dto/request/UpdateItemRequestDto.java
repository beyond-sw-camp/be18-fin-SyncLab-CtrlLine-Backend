package com.beyond.synclab.ctrlline.domain.item.dto.request;

import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UpdateItemRequestDto {
    private String itemCode;
    private String itemName;
    private String itemSpecification;
    private String itemUnit;
    private ItemStatus itemStatus;
    private Boolean isActive;
}
