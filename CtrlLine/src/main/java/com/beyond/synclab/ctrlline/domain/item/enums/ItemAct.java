package com.beyond.synclab.ctrlline.domain.item.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemAct {
    ACTIVE("사용"),
    INACTIVE("미사용");

    private final String description;
}
