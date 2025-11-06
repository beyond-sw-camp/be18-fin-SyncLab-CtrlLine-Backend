package com.beyond.synclab.ctrlline.domain.item.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemStatus {

    RAW_MATERIAL("원재료"),
    SUB_MATERIAL("부재료"),
    SEMI_FINISHED_PRODUCT("반제품"),
    FINISHED_PRODUCT("완제품");

    private final String description;

    @Override
    public String toString() {
        return this.name();
    }
}
