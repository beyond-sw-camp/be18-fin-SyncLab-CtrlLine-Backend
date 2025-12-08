package com.beyond.synclab.ctrlline.domain.optimization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizeCommitResponseDto {
    private int updatedCount;
    private String lineCode;
}