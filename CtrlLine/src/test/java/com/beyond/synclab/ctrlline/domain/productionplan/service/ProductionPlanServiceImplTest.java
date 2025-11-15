package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionPlanServiceImplTest {
    @InjectMocks
    private ProductionPlanServiceImpl productionPlanService;

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LineRepository lineRepository;

    @Mock
    private FactoryRepository factoryRepository;

    @Mock
    private ItemRepository itemRepository;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2099-01-01T00:00:00Z"), ZoneOffset.UTC);
    }

    @Test
    @DisplayName("유저 권한으로 생산 계획을 성공적으로 등록합니다.")
    void createProductionPlan_WhenUser_success() {

        // given
        LocalDate dueDateFromClock = LocalDate.now(fixedClock);
        LocalDateTime startTimeFromClock = LocalDateTime.now(fixedClock);
        LocalDateTime endTimeFromClock = LocalDateTime.now(fixedClock).plusHours(8);

        CreateProductionPlanRequestDto requestDto = CreateProductionPlanRequestDto.builder()
                .dueDate(dueDateFromClock)
                .status(ProductionPlans.PlanStatus.PENDING)
                .salesManagerNo("209901001")
                .productionManagerNo("209901002")
                .startTime(startTimeFromClock)
                .endTime(endTimeFromClock)
                .factoryCode("F001")
                .itemCode("ITEM001")
                .plannedQty(new BigDecimal("100"))
                .lineCode("LINE01")
                .remark("테스트 생산 계획")
                .build();

        Lines line = Lines.builder().id(1L).lineCode("LINE01").build();
        Users salesManager = Users.builder().id(10L).empNo("209901001").name("Sales").build();
        Users productionManager = Users.builder().id(20L).empNo("209901002").name("Production").build();
        Users requestUser = Users.builder().role(Users.UserRole.USER).build();
        Factories factory = Factories.builder().factoryCode("F001").build();
        Items item = Items.builder().itemCode("ITEM001").build();

        ProductionPlans savedPlan = ProductionPlans.builder()
                .id(999L)
                .line(line)
                .salesManager(salesManager)
                .productionManager(productionManager)
                .documentNo("DOC-TEST1234")
                .status(requestDto.getStatus())
                .dueDate(requestDto.getDueDate())
                .plannedQty(requestDto.getPlannedQty())
                .startTime(requestDto.getStartTime())
                .endTime(requestDto.getEndTime())
                .remark(requestDto.getRemark())
                .build();

        when(lineRepository.findBylineCode("LINE01")).thenReturn(Optional.of(line));
        when(userRepository.findByEmpNo("209901001")).thenReturn(Optional.of(salesManager));
        when(userRepository.findByEmpNo("209901002")).thenReturn(Optional.of(productionManager));
        when(factoryRepository.findByFactoryCode("F001")).thenReturn(Optional.of(factory));
        when(itemRepository.findByItemCode("ITEM001")).thenReturn(Optional.of(item));
        when(productionPlanRepository.save(any(ProductionPlans.class))).thenReturn(any());

        ProductionPlanResponseDto productionPlanResponseDto = productionPlanService.createProductionPlan(requestDto, requestUser);

        assertThat(productionPlanResponseDto.getEndTime()).isEqualTo(endTimeFromClock);
    }
}