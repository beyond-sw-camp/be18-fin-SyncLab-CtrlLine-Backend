package com.beyond.synclab.ctrlline.domain.productionplan.repository;

import com.beyond.synclab.ctrlline.domain.productionplan.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PlanQueryRepository {
    List<GetProductionPlanScheduleResponseDto> findSchedule(
            GetProductionPlanScheduleRequestDto requestDto
    );

    Page<GetProductionPlanListResponseDto> findPlanList(
            SearchProductionPlanCommand command,
            Pageable pageable
    );

    Optional<GetProductionPlanDetailResponseDto> findPlanDetail(Long planId);

    List<GetAllProductionPlanResponseDto> findAllPlans(
            GetAllProductionPlanRequestDto requestDto
    );
}
