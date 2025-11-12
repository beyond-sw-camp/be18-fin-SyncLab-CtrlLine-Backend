package com.beyond.synclab.ctrlline.domain.item.dto.request;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UpdateItemActRequestDto {
    private List<Long> itemIds;
    private Boolean isActive;
}
