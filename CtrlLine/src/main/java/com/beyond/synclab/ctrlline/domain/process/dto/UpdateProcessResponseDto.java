package com.beyond.synclab.ctrlline.domain.process.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class UpdateProcessResponseDto {
    private final String ProcessCode;
    private final String ProcessName;
    private final String userDepartment;
    private final String userName;
    private final String empNo;
    private final Boolean isActive;
    private final LocalDateTime updatedAt;
}
