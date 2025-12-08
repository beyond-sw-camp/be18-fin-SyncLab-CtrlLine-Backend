package com.beyond.synclab.ctrlline.domain.optimization.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationPreviewSnapshot implements Serializable {
    private String lineCode;
    private List<PlanSnapshot> assignments;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanSnapshot implements Serializable {
        private Long planId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
