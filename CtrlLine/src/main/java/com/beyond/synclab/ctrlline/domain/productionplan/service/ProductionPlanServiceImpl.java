package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanCreateRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProductionPlanServiceImpl implements ProductionPlanService {

    @Override
    @Transactional
    public ProductionPlanResponseDto enroll(ProductionPlanCreateRequestDto requestDto, Users user) {
        return null;
    }
}
