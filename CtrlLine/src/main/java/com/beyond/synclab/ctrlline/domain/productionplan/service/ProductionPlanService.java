package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanCreateRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;

public interface ProductionPlanService {

    ProductionPlanResponseDto enroll(ProductionPlanCreateRequestDto requestDto, Users user);
}
