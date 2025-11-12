package com.beyond.synclab.ctrlline.domain.productionplan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductionPlanServiceImplTest {
    @InjectMocks
    private ProductionPlanServiceImpl productionPlanService;

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @Test
    @DisplayName("유저 권한으로 생산 계획을 성공적으로 등록합니다.")
    void createProductionPlan_WhenUser_success() {

        // given
        CreateProductionPlanRequestDto requestDto = CreateProductionPlanRequestDto.builder()
            .lineCode("L001")
            .productionManagerNo(1L)
            .build();

        Users user = Users.builder()
                .email("hong123@test.com")
                .build();
        when(productionPlanRepository.findAllByLineCode("L001")).thenReturn(Collections.emptyList());

        ProductionPlanResponseDto productionPlanResponseDto = productionPlanService.createProductionPlan(requestDto, user);

        assertThat(productionPlanResponseDto.getEndTime()).isEqualTo(LocalDateTime.now());
    }
}