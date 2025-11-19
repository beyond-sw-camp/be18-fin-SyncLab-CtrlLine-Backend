package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanSearchCommand;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductionPlanService {

    ProductionPlanResponseDto createProductionPlan(CreateProductionPlanRequestDto requestDto, Users user);

    ProductionPlanDetailResponseDto getProductionPlan(Long planId);

    Page<ProductionPlanListResponseDto> getProductionPlanList(ProductionPlanSearchCommand searchCommand, Pageable pageable);
}
