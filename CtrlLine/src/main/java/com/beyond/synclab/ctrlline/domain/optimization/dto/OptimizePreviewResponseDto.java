package com.beyond.synclab.ctrlline.domain.optimization.dto;

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
public class OptimizePreviewResponseDto {
    private String previewKey; // Redis snapshot key
    private String lineCode;
    private List<PreviewPlanDto> plans;

    @Getter
    @Builder
    public static class PreviewPlanDto {
        private Long planId;
        private String documentNo;
        private LocalDateTime originalStartTime;
        private LocalDateTime optimizedStartTime;
        private LocalDateTime optimizedEndTime;
        private LocalDateTime dueDateTime;
        private boolean confirmed; // 원본 상태
        private boolean locked;    // MANAGER 모드에서 Confirmed는 true
    }
}
