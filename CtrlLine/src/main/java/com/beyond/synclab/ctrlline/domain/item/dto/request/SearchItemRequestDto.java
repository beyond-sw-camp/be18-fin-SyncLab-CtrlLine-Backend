package com.beyond.synclab.ctrlline.domain.item.dto.request;

import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchItemRequestDto {

    private String itemCode;
    private String itemName;
    private String itemSpecification;
    private ItemStatus itemStatus;
    private Boolean isActive;
}
