package com.beyond.synclab.ctrlline.domain.process.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateProcessActResponseDto {
    private final Boolean isActive;

    public static UpdateProcessActResponseDto of(Boolean isActive) {
        return UpdateProcessActResponseDto.builder()
                .isActive(isActive)
                .build();
    }
}
