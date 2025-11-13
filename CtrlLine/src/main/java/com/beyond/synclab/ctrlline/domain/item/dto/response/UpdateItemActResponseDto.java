package com.beyond.synclab.ctrlline.domain.item.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateItemActResponseDto {
    private final Boolean isActive;

    public static UpdateItemActResponseDto of(Boolean isActive) {
        return UpdateItemActResponseDto.builder()
                .isActive(isActive)
                .build();
    }
}
