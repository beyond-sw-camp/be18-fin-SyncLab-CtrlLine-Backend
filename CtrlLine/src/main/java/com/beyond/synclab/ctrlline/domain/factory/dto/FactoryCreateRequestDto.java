package com.beyond.synclab.ctrlline.domain.factory.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FactoryCreateRequestDto {
    private String factoryCode;
    private String factoryName;
    private String department;
    private String name;
    private boolean isActive;
}
