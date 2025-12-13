package com.beyond.synclab.ctrlline.domain.item.dto.request;

import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SearchItemRequestDto {

    private String itemCode;
    private String itemName;
    private String itemSpecification;
    private ItemStatus itemStatus;
    private Boolean isActive;
    private String factoryCode;
}
