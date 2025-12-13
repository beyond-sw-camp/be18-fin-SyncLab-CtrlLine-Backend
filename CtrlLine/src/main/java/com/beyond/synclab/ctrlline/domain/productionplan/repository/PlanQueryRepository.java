package com.beyond.synclab.ctrlline.domain.productionplan.repository;

import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanScheduleRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanScheduleResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.SearchProductionPlanCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PlanQueryRepository {
    List<GetProductionPlanScheduleResponseDto> findSchedule(
            GetProductionPlanScheduleRequestDto requestDto
    );

    Page<GetProductionPlanListResponseDto> findPlanList(
            SearchProductionPlanCommand command,
            Pageable pageable
    );
}
