package com.beyond.synclab.ctrlline.domain.optimization.service;

import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizeCommitResponseDto;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizePreviewResponseDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;

public interface ProductionPlanOptimizationService {
    OptimizePreviewResponseDto previewOptimization(String lineCode, Users user);

    OptimizeCommitResponseDto commitOptimization(String lineCode, String previewKey, Users user);
}
