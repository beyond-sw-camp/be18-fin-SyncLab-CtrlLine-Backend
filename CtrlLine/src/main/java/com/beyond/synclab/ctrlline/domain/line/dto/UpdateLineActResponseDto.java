package com.beyond.synclab.ctrlline.domain.line.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateLineActResponseDto {
    private final Boolean isActive;

    public static UpdateLineActResponseDto of(Boolean isActive) {
        return UpdateLineActResponseDto.builder()
                .isActive(isActive)
                .build();
    }
}
