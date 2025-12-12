package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import lombok.Builder;

@Builder
public record UpdateProductionPlanCommitRequestDto(String previewKey) {
}
