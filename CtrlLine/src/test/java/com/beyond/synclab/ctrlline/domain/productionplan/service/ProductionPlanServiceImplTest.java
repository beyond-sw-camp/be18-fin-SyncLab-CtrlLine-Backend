package com.beyond.synclab.ctrlline.domain.productionplan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductionPlanServiceImplTest {

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

    private ProductionPlanServiceImpl productionPlanService;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        // 고정된 시간
        fixedClock = Clock.fixed(
            Instant.parse("2099-01-01T00:00:00Z"),
            ZoneId.systemDefault()
        );

        // 서비스에 Mock + FixedClock 직접 주입
        productionPlanService = new ProductionPlanServiceImpl(
            productionPlanRepository,
            userRepository,
            lineRepository,
            factoryRepository,
            itemRepository,
            fixedClock
        );
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

        when(lineRepository.findBylineCode("LINE01")).thenReturn(Optional.of(line));
        when(userRepository.findByEmpNo("209901001")).thenReturn(Optional.of(salesManager));
        when(userRepository.findByEmpNo("209901002")).thenReturn(Optional.of(productionManager));
        when(factoryRepository.findByFactoryCode("F001")).thenReturn(Optional.of(factory));
        when(itemRepository.findByItemCode("ITEM001")).thenReturn(Optional.of(item));
        when(productionPlanRepository.save(any(ProductionPlans.class))).thenReturn(any());

        ProductionPlanResponseDto productionPlanResponseDto = productionPlanService.createProductionPlan(requestDto, requestUser);

        assertThat(productionPlanResponseDto.getEndTime()).isEqualTo(endTimeFromClock);
    }

    @Test
    @DisplayName("전표번호 생성 성공 - 신규 전표번호가 첫번째면 1번이어야한다")
    void createDocumentNo_whenFirst_thenOne() {
        // given
        String prefix = "2099/01/01";

        // repository mock 동작 정의 — 조회 결과 없음
        Mockito.when(productionPlanRepository.findByDocumentNoByPrefix(prefix))
            .thenReturn(List.of());

        // when
        String docNo = productionPlanService.createDocumentNo();

        // then
        Assertions.assertEquals("2099/01/01-1", docNo);
    }

    @Test
    @DisplayName("전표번호 생성 성공 - 기존 전표번호가 있으면 마지막번호에서 증가해야한다")
    void createDocumentNo_whenExists_thenLastNumPlus() {
        // given
        String prefix = "2099/01/01";

        // 이미 2025/11/17-3 까지 있다고 가정
        Mockito.when(productionPlanRepository.findByDocumentNoByPrefix(prefix))
            .thenReturn(List.of("2099/01/01-3", "2099/01/01-2", "2099/01/01-1"));

        // when
        String docNo = productionPlanService.createDocumentNo();

        // then
        Assertions.assertEquals("2099/01/01-4", docNo);
    }
}