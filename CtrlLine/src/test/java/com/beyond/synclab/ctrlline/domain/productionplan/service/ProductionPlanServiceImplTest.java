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
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.itemline.repository.ItemLineRepository;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.errorcode.LineErrorCode;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanSearchCommand;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionplan.errorcode.ProductionPlanErrorCode;
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
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ProductionPlanServiceImplTest {

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LineRepository lineRepository;

    @Mock
    private ItemLineRepository itemLineRepository;

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
            itemLineRepository,
            itemRepository,
            equipmentRepository,
            fixedClock
        );
    }

    private Lines line(Long id) {
        Factories factories = Factories.builder().factoryName("1공장").factoryCode("F001").build();
        return Lines.builder().id(id).lineName("라인").lineCode("LINE01").factory(factories).build();
    }

    private Users user(String empNo) {
        return Users.builder().name("유저").empNo(empNo).build();
    }

    private Items item(Long id) {
        return Items.builder().id(id).itemName("아이템").itemCode("ITEM001").build();
    }

    private ItemsLines itemLine(Lines line, Items item) {
        return ItemsLines.builder()
            .id(1L)
            .line(line)
            .lineId(line.getId())
            .item(item)
            .itemId(item.getId())
            .build();
    }

    private Equipments equipment(BigDecimal ppm) {
        return Equipments.builder()
            .equipmentPpm(ppm)
            .totalCount(new BigDecimal("1000"))
            .defectiveCount(new BigDecimal("10"))
            .build();
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

        Lines line = line(1L);
        Items item = item(1L);
        ItemsLines itemsLines = itemLine(line, item);
        Users salesManager = Users.builder().id(10L).empNo("209901001").name("Sales").build();
        Users productionManager = Users.builder().id(20L).empNo("209901002").name("Production").build();
        Users requestUser = Users.builder().role(Users.UserRole.USER).build();
        Factories factory = Factories.builder().factoryCode("F001").build();

        Equipments equipment = equipment(new BigDecimal("100"));

        when(userRepository.findByEmpNo("209901001")).thenReturn(Optional.of(salesManager));
        when(userRepository.findByEmpNo("209901002")).thenReturn(Optional.of(productionManager));
        when(lineRepository.findBylineCode("LINE01")).thenReturn(Optional.of(line));
        when(factoryRepository.findByFactoryCode("F001")).thenReturn(Optional.of(factory));
        when(itemRepository.findByItemCode("ITEM001")).thenReturn(Optional.of(item));
        when(itemLineRepository.findByLineIdAndItemId(1L, 1L))
            .thenReturn(Optional.of(itemsLines));
        when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(List.of(equipment));
        when(productionPlanRepository.findByLineCodeAndStatusInAndEndTimeAfterOrderByCreatedAtDesc(
            eq("LINE01"), anyList(), any(LocalDateTime.class)
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

        CreateProductionPlanRequestDto requestDto = CreateProductionPlanRequestDto.builder()
            .dueDate(LocalDate.now(fixedClock))
            .plannedQty(new BigDecimal("100"))
            .lineCode("LINE01")
            .salesManagerNo("209901001")
            .productionManagerNo("209901002")
            .factoryCode("F001")
            .itemCode("ITEM001")
            .build();

        Lines line = Lines.builder().id(1L).lineCode("LINE01").build();
        Items item = Items.builder().id(1L).itemCode("ITEM001").build();
        ItemsLines itemsLines = ItemsLines.builder().id(1L).lineId(1L).line(line).item(item).itemId(1L).build();
        Users salesManager = Users.builder().empNo("209901001").build();
        Users productionManager = Users.builder().empNo("209901002").build();
        Users requestUser = Users.builder().role(Users.UserRole.USER).build();
        Factories factory = Factories.builder().factoryCode("F001").build();
        Equipments equipment = Equipments.builder()
            .equipmentPpm(new BigDecimal("100"))
            .totalCount(new BigDecimal("1000"))
            .defectiveCount(new BigDecimal("10"))
            .build();

        when(lineRepository.findBylineCode("LINE01")).thenReturn(Optional.of(line));
        when(userRepository.findByEmpNo("209901001")).thenReturn(Optional.of(salesManager));
        when(userRepository.findByEmpNo("209901002")).thenReturn(Optional.of(productionManager));
        when(itemLineRepository.findByLineIdAndItemId(1L, 1L))
            .thenReturn(Optional.of(itemsLines));
        when(factoryRepository.findByFactoryCode("F001")).thenReturn(Optional.of(factory));
        when(itemRepository.findByItemCode("ITEM001")).thenReturn(Optional.of(item));
        when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(List.of(equipment));
        when(productionPlanRepository.findByLineCodeAndStatusInAndEndTimeAfterOrderByCreatedAtDesc(
            eq("LINE01"), anyList(), any(LocalDateTime.class)
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

        Lines line = Lines.builder().id(1L).lineCode("LINE01").build();
        Users salesManager = Users.builder().id(10L).empNo("209901001").name("Sales").build();
        Users productionManager = Users.builder().id(20L).empNo("209901002").name("Production").build();
        Factories factory = Factories.builder().factoryCode("F001").build();
        Items item = Items.builder().id(1L).itemCode("ITEM001").build();
        ItemsLines itemsLines = ItemsLines.builder().id(1L).lineId(1L).line(line).item(item).itemId(1L).build();

        // repository mocking
        when(lineRepository.findBylineCode("LINE01")).thenReturn(Optional.of(line));
        when(userRepository.findByEmpNo("209901001")).thenReturn(Optional.of(salesManager));
        when(userRepository.findByEmpNo("209901002")).thenReturn(Optional.of(productionManager));
        when(factoryRepository.findByFactoryCode("F001")).thenReturn(Optional.of(factory));
        when(itemRepository.findByItemCode("ITEM001")).thenReturn(Optional.of(item));
        when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(Collections.emptyList());
        when(itemLineRepository.findByLineIdAndItemId(1L, 1L))
            .thenReturn(Optional.of(itemsLines));

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
        Items item = Items.builder().id(1L).itemCode("ITEM001").build();
        ItemsLines itemsLines = ItemsLines.builder().id(1L).lineId(1L).line(line).item(item).itemId(1L).build();

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
        when(itemLineRepository.findByLineIdAndItemId(1L, 1L))
            .thenReturn(Optional.of(itemsLines));

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
        Items item = Items.builder().id(1L).itemCode("ITEM001").build();
        ItemsLines itemsLines = ItemsLines.builder().id(1L).lineId(1L).line(line).item(item).itemId(1L).build();

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
            eq("LINE01"), anyList(), any(LocalDateTime.class)
        )).thenReturn(Optional.empty());
        when(productionPlanRepository.findByDocumentNoByPrefix(anyString())).thenReturn(List.of());
        when(productionPlanRepository.save(any(ProductionPlans.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(itemLineRepository.findByLineIdAndItemId(1L, 1L))
            .thenReturn(Optional.of(itemsLines));

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

    @Test
    @DisplayName("생산 계획 상세 조회에 성공한다.")
    void getProductionPlan_success() {
        // given
        Long planId = 1L;

        Lines line = line(1L);
        Items item = item(1L);
        ItemsLines itemLine = itemLine(line, item);
        Users salesManager = user("209901001");
        Users productionManager = user("209901002");

        ProductionPlans productionPlans = ProductionPlans.builder()
            .id(planId)
            .salesManager(salesManager)
            .productionManager(productionManager)
            .itemLine(itemLine)
            .documentNo("2099/01/01-1")
            .plannedQty(new BigDecimal("500"))
            .build();

        when(productionPlanRepository.findById(planId))
            .thenReturn(Optional.of(productionPlans));

        // when
        ProductionPlanDetailResponseDto response =
            productionPlanService.getProductionPlan(planId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getPlanDocumentNo()).isEqualTo("2099/01/01-1");
        assertThat(response.getItemCode()).isEqualTo("ITEM001");
        assertThat(response.getFactoryCode()).isEqualTo("F001");

        verify(productionPlanRepository, times(1)).findById(planId);
    }

    @Test
    @DisplayName("생산 계획 ID가 존재하지 않을 경우 예외를 반환한다.")
    void getProductionPlan_notFound() {
        // given
        Long planId = 999L;

        when(productionPlanRepository.findById(planId))
            .thenReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> productionPlanService.getProductionPlan(planId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND.getMessage());

        verify(productionPlanRepository, times(1)).findById(planId);
    }

    @Test
    @DisplayName("생산 계획 목록 조회 - Specification 정상 적용 및 DTO 매핑")
    void getProductionPlanList_success() {
        // given
        ProductionPlanSearchCommand command = ProductionPlanSearchCommand.builder()
            .status(PlanStatus.PENDING)
            .factoryName("1공장")
            .salesManagerName("유저")
            .productionManagerName("유저")
            .itemName("아이템")
            .dueDate(LocalDate.now(fixedClock))
            .startTime(LocalDateTime.now(fixedClock).minusDays(1))
            .endTime(LocalDateTime.now(fixedClock).plusDays(1))
            .build();

        Pageable pageable = PageRequest.of(0, 10);

        ProductionPlans mockEntity = ProductionPlans.builder()
            .id(1L)
            .documentNo("PP-001")
            .status(PlanStatus.PENDING)
            .plannedQty(BigDecimal.valueOf(200))
            .dueDate(LocalDate.of(2099, 1, 10))
            .itemLine(itemLine(line(1L), item(1L)))
            .salesManager(user("209901001"))
            .productionManager(user("209901002"))
            .remark("비고")
            .build();

        Page<ProductionPlans> mockPage = new PageImpl<>(List.of(mockEntity), pageable, 1);

        when(productionPlanRepository.findAll(ArgumentMatchers.<Specification<ProductionPlans>>any(), any(Pageable.class)))
            .thenReturn(mockPage);

        // when
        Page<ProductionPlanListResponseDto> result =
            productionPlanService.getProductionPlanList(command, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        ProductionPlanListResponseDto dto = result.getContent().getFirst();

        assertThat(dto.getId()).isEqualTo(mockEntity.getId());
        assertThat(dto.getDocumentNo()).isEqualTo(mockEntity.getDocumentNo());
        assertThat(dto.getStatus()).isEqualTo(mockEntity.getStatus());
        assertThat(dto.getPlannedQty()).isEqualTo(mockEntity.getPlannedQty());
        assertThat(dto.getDueDate()).isEqualTo(mockEntity.getDueDate());
        assertThat(dto.getRemark()).isEqualTo(mockEntity.getRemark());

        // repository 호출 검증
        verify(productionPlanRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<ProductionPlans>>any(), any(Pageable.class));
    }

    @Test
    @DisplayName("생산 계획 목록 조회 - 기본 documentNo DESC 정렬 적용")
    void getProductionPlanList_defaultSortApplied() {
        // given
        ProductionPlanSearchCommand command = ProductionPlanSearchCommand.builder()
            .status(PlanStatus.PENDING)
            .build();

        // 클라이언트 Pageable에 sort 미지정
        Pageable pageable = PageRequest.of(0, 10);

        ProductionPlans mockEntity = ProductionPlans.builder()
            .id(1L)
            .documentNo("PP-002")
            .status(PlanStatus.PENDING)
            .plannedQty(BigDecimal.valueOf(100))
            .dueDate(LocalDate.of(2099, 1, 10))
            .itemLine(itemLine(line(1L), item(1L)))
            .salesManager(user("209901001"))
            .productionManager(user("209901002"))
            .remark("비고")
            .build();

        Page<ProductionPlans> mockPage = new PageImpl<>(List.of(mockEntity), pageable, 1);
        when(productionPlanRepository.findAll(ArgumentMatchers.<Specification<ProductionPlans>>any(), any(Pageable.class)))
            .thenReturn(mockPage);

        // when
        Page<ProductionPlanListResponseDto> result =
            productionPlanService.getProductionPlanList(command, pageable);

        // then
        // DTO 매핑 검증
        assertThat(result.getTotalElements()).isEqualTo(1);
        ProductionPlanListResponseDto dto = result.getContent().getFirst();
        assertThat(dto.getId()).isEqualTo(mockEntity.getId());
        assertThat(dto.getDocumentNo()).isEqualTo(mockEntity.getDocumentNo());

        // Pageable에 기본 Sort가 documentNo DESC로 적용됐는지 검증
        verify(productionPlanRepository).findAll(
            ArgumentMatchers.<Specification<ProductionPlans>>any(),
            ArgumentMatchers.<Pageable>argThat( p -> {
                Sort.Order order = p.getSort().getOrderFor("documentNo");
                return order != null && order.getDirection() == Sort.Direction.DESC;
            }
            ));
    }

    @Test
    @DisplayName("생산 계획 목록 조회 - 계획 수량 오름차순 확인")
    void getProductionPlanList_clientSortMerged() {
        // given
        ProductionPlanSearchCommand command = ProductionPlanSearchCommand.builder()
            .status(PlanStatus.PENDING)
            .build();

        // 클라이언트가 name ASC 정렬 요청
        Pageable pageable = PageRequest.of(0, 10, Sort.by("plannedQty").ascending());

        ProductionPlans mockEntity = ProductionPlans.builder()
            .id(1L)
            .documentNo("PP-003")
            .status(PlanStatus.PENDING)
            .plannedQty(BigDecimal.valueOf(200))
            .dueDate(LocalDate.of(2099, 1, 10))
            .itemLine(itemLine(line(1L), item(1L)))
            .salesManager(user("209901001"))
            .productionManager(user("209901002"))
            .remark("비고")
            .build();

        Page<ProductionPlans> mockPage = new PageImpl<>(List.of(mockEntity), pageable, 1);
        when(productionPlanRepository.findAll(ArgumentMatchers.<Specification<ProductionPlans>>any(), any(Pageable.class)))
            .thenReturn(mockPage);

        // when
        Page<ProductionPlanListResponseDto> result =
            productionPlanService.getProductionPlanList(command, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        ProductionPlanListResponseDto dto = result.getContent().getFirst();
        assertThat(dto.getId()).isEqualTo(mockEntity.getId());

        // 클라이언트 Sort(plannedQty ASC)로 되었는지 확인
        verify(productionPlanRepository).findAll(
            ArgumentMatchers.<Specification<ProductionPlans>>any(),
            ArgumentMatchers.<Pageable>argThat(p -> {
                Sort sort = p.getSort();
                Sort.Order plannedQtyOrder = sort.getOrderFor("plannedQty");

                return plannedQtyOrder != null
                    && plannedQtyOrder.getDirection() == Sort.Direction.ASC;
            }));
    }

    @Test
    @DisplayName("생산 계획 목록 조회 - 엣지케이스: 빈 데이터")
    void getProductionPlanList_emptyResult() {
        // given
        ProductionPlanSearchCommand command = ProductionPlanSearchCommand.builder().build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProductionPlans> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(productionPlanRepository.findAll(ArgumentMatchers.<Specification<ProductionPlans>>any(), any(Pageable.class)))
            .thenReturn(emptyPage);

        // when
        Page<ProductionPlanListResponseDto> result =
            productionPlanService.getProductionPlanList(command, pageable);

        // then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }
}