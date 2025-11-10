package com.beyond.synclab.ctrlline.domain.factory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FactorySearchDto {
    private final Boolean isActive;
    private final String factoryName;
    private final String factoryCode;
    private final String name;
    private final String department;
}
