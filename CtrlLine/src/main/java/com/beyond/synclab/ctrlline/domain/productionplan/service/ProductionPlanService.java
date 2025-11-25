package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.SearchProductionPlanCommand;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductionPlanService {

    GetProductionPlanResponseDto createProductionPlan(CreateProductionPlanRequestDto requestDto, Users user);

    GetProductionPlanDetailResponseDto getProductionPlan(Long planId);

    Page<GetProductionPlanListResponseDto> getProductionPlanList(
        SearchProductionPlanCommand searchCommand, Pageable pageable);

    GetProductionPlanResponseDto updateProductionPlan(UpdateProductionPlanRequestDto requestDto, Long planId, Users user);

    List<GetAllProductionPlanResponseDto> getAllProductionPlan(GetAllProductionPlanRequestDto requestDto);
}
