package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.DeleteProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanBoundaryResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanEndTimeRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanEndTimeResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanScheduleRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanScheduleResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.PlanScheduleChangeResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.SearchProductionPlanCommand;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanStatusResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.UpdateProductionPlanStatusRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductionPlanService {

    PlanScheduleChangeResponseDto createProductionPlan(CreateProductionPlanRequestDto requestDto, Users user);

    GetProductionPlanDetailResponseDto getProductionPlan(Long planId);

    Page<GetProductionPlanListResponseDto> getProductionPlanList(
        SearchProductionPlanCommand searchCommand, Pageable pageable);

    PlanScheduleChangeResponseDto updateProductionPlan(UpdateProductionPlanRequestDto requestDto, Long planId, Users user);

    List<GetAllProductionPlanResponseDto> getAllProductionPlan(GetAllProductionPlanRequestDto requestDto);

    List<GetProductionPlanScheduleResponseDto> getProductionPlanSchedule(GetProductionPlanScheduleRequestDto requestDto);

    GetProductionPlanEndTimeResponseDto getProductionPlanEndTime(GetProductionPlanEndTimeRequestDto requestDto);

    UpdateProductionPlanStatusResponseDto updateProductionPlanStatus(
        UpdateProductionPlanStatusRequestDto requestDto);

    void deleteProductionPlan(Long planId, Users user);

    void deleteProductionPlans(DeleteProductionPlanRequestDto requestDto, Users user);

    GetProductionPlanBoundaryResponseDto getPlanBoundaries(String factoryCode, String lineCode);
}
