package com.beyond.synclab.ctrlline.domain.process.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class UpdateProcessRequestDto {
    private String userName;
    private String empNo;
    private Boolean isActive;
}
