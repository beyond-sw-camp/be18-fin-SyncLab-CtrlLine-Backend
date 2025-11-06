package com.beyond.synclab.ctrlline.domain.factory.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FactoryCreateResponseDto {
    private final String factoryCode;
    private final String factoryName;
    private final String department;
    private final String name;
}
