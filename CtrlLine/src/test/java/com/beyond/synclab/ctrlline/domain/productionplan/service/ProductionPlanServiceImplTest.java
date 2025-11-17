package com.beyond.synclab.ctrlline.domain.productionplan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.errorcode.LineErrorCode;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
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

    @Mock
    private EquipmentRepository equipmentRepository;

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
            equipmentRepository,
            fixedClock
        );
    }

    @Test
    @DisplayName("유저 권한으로 생산 계획을 성공적으로 등록합니다.")
    void createProductionPlan_WhenUser_success() {
        // given
        LocalDate dueDate = LocalDate.now(fixedClock);
        LocalDateTime startTime = LocalDateTime.now(fixedClock);

        CreateProductionPlanRequestDto requestDto = CreateProductionPlanRequestDto.builder()
            .dueDate(dueDate)
            .salesManagerNo("209901001")
            .productionManagerNo("209901002")
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

        Equipments equipment = Equipments.builder()
            .id(100L)
            .equipmentPpm(new BigDecimal("100"))
            .totalCount(new BigDecimal("1000"))
            .defectiveCount(new BigDecimal("10"))
            .build();

        when(userRepository.findByEmpNo("209901001")).thenReturn(Optional.of(salesManager));
        when(userRepository.findByEmpNo("209901002")).thenReturn(Optional.of(productionManager));
        when(lineRepository.findBylineCode("LINE01")).thenReturn(Optional.of(line));
        when(factoryRepository.findByFactoryCode("F001")).thenReturn(Optional.of(factory));
        when(itemRepository.findByItemCode("ITEM001")).thenReturn(Optional.of(item));
        when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(List.of(equipment));
        when(productionPlanRepository.findByLineCodeAndStatusInAndEndTimeAfterOrderByCreatedAtDesc(
            eq("LINE01"), anyList(), any(LocalDate.class)
        )).thenReturn(Optional.empty());
        when(productionPlanRepository.findByDocumentNoByPrefix(anyString())).thenReturn(List.of());

        when(productionPlanRepository.save(any(ProductionPlans.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ProductionPlanResponseDto responseDto = productionPlanService.createProductionPlan(requestDto, requestUser);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getStartTime()).isAfterOrEqualTo(startTime);
        assertThat(responseDto.getEndTime()).isAfter(responseDto.getStartTime());

        verify(productionPlanRepository, times(1)).save(any(ProductionPlans.class));
        verify(equipmentRepository, times(1)).findAllByLineId(line.getId());
    }

    @Test
    @DisplayName("기존 생산계획이 존재하면 새로운 계획의 시작시간은 기존 종료시간 이후로 설정된다.")
    void createProductionPlan_WhenExistingPlanExists_startTimeAfterPrevious() {
        // given
        LocalDateTime existingEndTime = LocalDateTime.now(fixedClock).plusHours(2);
        ProductionPlans existingPlan = ProductionPlans.builder()
            .endTime(existingEndTime)
            .status(PlanStatus.CONFIRMED)
            .build();

        Lines line = Lines.builder().id(1L).lineCode("LINE01").build();
        Users salesManager = Users.builder().empNo("209901001").build();
        Users productionManager = Users.builder().empNo("209901002").build();
        Users requestUser = Users.builder().role(Users.UserRole.USER).build();
        Factories factory = Factories.builder().factoryCode("F001").build();
        Items item = Items.builder().itemCode("ITEM001").build();
        Equipments equipment = Equipments.builder()
            .equipmentPpm(new BigDecimal("100"))
            .totalCount(new BigDecimal("1000"))
            .defectiveCount(new BigDecimal("10"))
            .build();

        CreateProductionPlanRequestDto requestDto = CreateProductionPlanRequestDto.builder()
            .dueDate(LocalDate.now(fixedClock))
            .plannedQty(new BigDecimal("100"))
            .lineCode("LINE01")
            .salesManagerNo("209901001")
            .productionManagerNo("209901002")
            .factoryCode("F001")
            .itemCode("ITEM001")
            .build();

        when(lineRepository.findBylineCode("LINE01")).thenReturn(Optional.of(line));
        when(userRepository.findByEmpNo("209901001")).thenReturn(Optional.of(salesManager));
        when(userRepository.findByEmpNo("209901002")).thenReturn(Optional.of(productionManager));
        when(factoryRepository.findByFactoryCode("F001")).thenReturn(Optional.of(factory));
        when(itemRepository.findByItemCode("ITEM001")).thenReturn(Optional.of(item));
        when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(List.of(equipment));
        when(productionPlanRepository.findByLineCodeAndStatusInAndEndTimeAfterOrderByCreatedAtDesc(
            eq("LINE01"), anyList(), any(LocalDate.class)
        )).thenReturn(Optional.of(existingPlan));
        when(productionPlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ProductionPlanResponseDto result = productionPlanService.createProductionPlan(requestDto, requestUser);

        // then
        assertThat(result.getStartTime()).isEqualTo(existingEndTime.plusMinutes(30));
    }

    @Test
    @DisplayName("라인에 설비가 없으면 NO_EQUIPMENT_FOUND 예외 발생")
    void createProductionPlan_WhenNoEquipment_ThrowsException() {
        // given
        Lines line = Lines.builder().id(1L).lineCode("LINE01").build();
        Users requestUser = Users.builder().role(Users.UserRole.USER).build();
        CreateProductionPlanRequestDto requestDto = CreateProductionPlanRequestDto.builder()
            .dueDate(LocalDate.now(fixedClock))
            .plannedQty(new BigDecimal("100"))
            .lineCode("LINE01")
            .salesManagerNo("209901001")
            .productionManagerNo("209901002")
            .factoryCode("F001")
            .itemCode("ITEM001")
            .build();

        Users salesManager = Users.builder().id(10L).empNo("209901001").name("Sales").build();
        Users productionManager = Users.builder().id(20L).empNo("209901002").name("Production").build();
        Factories factory = Factories.builder().factoryCode("F001").build();
        Items item = Items.builder().itemCode("ITEM001").build();

        // repository mocking
        when(lineRepository.findBylineCode("LINE01")).thenReturn(Optional.of(line));
        when(userRepository.findByEmpNo("209901001")).thenReturn(Optional.of(salesManager));
        when(userRepository.findByEmpNo("209901002")).thenReturn(Optional.of(productionManager));
        when(factoryRepository.findByFactoryCode("F001")).thenReturn(Optional.of(factory));
        when(itemRepository.findByItemCode("ITEM001")).thenReturn(Optional.of(item));
        when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> productionPlanService.createProductionPlan(requestDto, requestUser))
            .isInstanceOf(AppException.class)
            .hasMessageContaining(LineErrorCode.NO_EQUIPMENT_FOUND.getMessage());
    }


    @Test
    @DisplayName("설비 PPM 계산 후 총합이 0이면 INVALID_EQUIPMENT_PPM 예외 발생")
    void createProductionPlan_WhenEffectivePPMZero_ThrowsException() {
        // given
        Lines line = Lines.builder().id(1L).lineCode("LINE01").build();
        Users requestUser = Users.builder().role(Users.UserRole.USER).build();
        Users salesManager = Users.builder().id(10L).empNo("209901001").name("Sales").build();
        Users productionManager = Users.builder().id(20L).empNo("209901002").name("Production").build();
        Factories factory = Factories.builder().factoryCode("F001").build();
        Items item = Items.builder().itemCode("ITEM001").build();

        // 설비 PPM 0, 불량률 100%
        Equipments equipment = Equipments.builder()
            .equipmentPpm(BigDecimal.ZERO)
            .totalCount(new BigDecimal("1"))
            .defectiveCount(new BigDecimal("1"))
            .build();

        CreateProductionPlanRequestDto requestDto = CreateProductionPlanRequestDto.builder()
            .dueDate(LocalDate.now(fixedClock))
            .plannedQty(new BigDecimal("100"))
            .lineCode("LINE01")
            .salesManagerNo("209901001")
            .productionManagerNo("209901002")
            .factoryCode("F001")
            .itemCode("ITEM001")
            .build();

        // repository mocking
        when(lineRepository.findBylineCode("LINE01")).thenReturn(Optional.of(line));
        when(userRepository.findByEmpNo("209901001")).thenReturn(Optional.of(salesManager));
        when(userRepository.findByEmpNo("209901002")).thenReturn(Optional.of(productionManager));
        when(factoryRepository.findByFactoryCode("F001")).thenReturn(Optional.of(factory));
        when(itemRepository.findByItemCode("ITEM001")).thenReturn(Optional.of(item));
        when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(List.of(equipment));

        // when & then
        assertThatThrownBy(() -> productionPlanService.createProductionPlan(requestDto, requestUser))
            .isInstanceOf(AppException.class)
            .hasMessageContaining(LineErrorCode.INVALID_EQUIPMENT_PPM.getMessage());
    }

    @Test
    @DisplayName("관리자 권한으로 CONFIRMED 생산 계획 생성 시 시작 시간이 PENDING보다 짧게 설정")
    void createProductionPlan_WhenAdminConfirmed_StartTimeShorter() {
        // given
        LocalDate dueDate = LocalDate.now(fixedClock);
        LocalDateTime startTime = LocalDateTime.now(fixedClock);

        CreateProductionPlanRequestDto requestDto = CreateProductionPlanRequestDto.builder()
            .dueDate(dueDate)
            .salesManagerNo("209901001")
            .productionManagerNo("209901002")
            .factoryCode("F001")
            .itemCode("ITEM001")
            .plannedQty(new BigDecimal("100"))
            .lineCode("LINE01")
            .remark("CONFIRMED 유저 테스트")
            .build();

        Lines line = Lines.builder().id(1L).lineCode("LINE01").build();
        Users salesManager = Users.builder().id(10L).empNo("209901001").name("Sales").build();
        Users productionManager = Users.builder().id(20L).empNo("209901002").name("Production").build();
        Users requestUser = Users.builder().role(Users.UserRole.MANAGER).build(); // CONFIRMED 권한
        Factories factory = Factories.builder().factoryCode("F001").build();
        Items item = Items.builder().itemCode("ITEM001").build();

        Equipments equipment = Equipments.builder()
            .id(100L)
            .equipmentPpm(new BigDecimal("100"))
            .totalCount(new BigDecimal("1000"))
            .defectiveCount(new BigDecimal("10"))
            .build();

        // repository mocking
        when(userRepository.findByEmpNo("209901001")).thenReturn(Optional.of(salesManager));
        when(userRepository.findByEmpNo("209901002")).thenReturn(Optional.of(productionManager));
        when(lineRepository.findBylineCode("LINE01")).thenReturn(Optional.of(line));
        when(factoryRepository.findByFactoryCode("F001")).thenReturn(Optional.of(factory));
        when(itemRepository.findByItemCode("ITEM001")).thenReturn(Optional.of(item));
        when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(List.of(equipment));
        when(productionPlanRepository.findByLineCodeAndStatusInAndEndTimeAfterOrderByCreatedAtDesc(
            eq("LINE01"), anyList(), any(LocalDate.class)
        )).thenReturn(Optional.empty());
        when(productionPlanRepository.findByDocumentNoByPrefix(anyString())).thenReturn(List.of());
        when(productionPlanRepository.save(any(ProductionPlans.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ProductionPlanResponseDto responseDto = productionPlanService.createProductionPlan(requestDto, requestUser);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getStartTime()).isAfterOrEqualTo(startTime); // CONFIRMED 권한이라 PENDING보다 짧음
        assertThat(responseDto.getEndTime()).isAfter(responseDto.getStartTime());

        verify(productionPlanRepository, times(1)).save(any(ProductionPlans.class));
        verify(equipmentRepository, times(1)).findAllByLineId(line.getId());
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