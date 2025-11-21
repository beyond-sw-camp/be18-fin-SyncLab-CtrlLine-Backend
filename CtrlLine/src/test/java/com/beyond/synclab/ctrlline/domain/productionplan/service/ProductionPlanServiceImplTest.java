package com.beyond.synclab.ctrlline.domain.productionplan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
import com.beyond.synclab.ctrlline.domain.itemline.errorcode.ItemLineErrorCode;
import com.beyond.synclab.ctrlline.domain.itemline.repository.ItemLineRepository;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.errorcode.LineErrorCode;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.SearchProductionPlanCommand;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionplan.errorcode.ProductionPlanErrorCode;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import com.beyond.synclab.ctrlline.domain.user.errorcode.UserErrorCode;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("생산계획 서비스 테스트")
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

    private Factories factory() {
        return Factories.builder()
            .id(1L)
            .factoryCode("F001")
            .build();
    }

    private Lines line(Long id) {
        Factories factories = factory();
        return Lines.builder()
            .id(id)
            .lineName("라인")
            .lineCode("LINE01")
            .factoryId(factories.getId())
            .factory(factories)
            .build();
    }

    private Lines line() {
        Factories factories = factory();
        return Lines.builder()
            .id(1L)
            .lineName("라인")
            .lineCode("LINE01")
            .factoryId(factories.getId())
            .factory(factories)
            .build();
    }

    private Users user(String empNo) {
        return Users.builder()
            .name("유저")
            .role(UserRole.MANAGER)
            .empNo(empNo)
            .build();
    }

    private Users salesManager() {
        return Users.builder()
            .id(1L)
            .name("영업담당자1")
            .role(UserRole.MANAGER)
            .empNo("209901001")
            .build();
    }

    private Users productionManager() {
        return Users.builder()
            .id(2L)
            .name("생산담당자1")
            .role(UserRole.MANAGER)
            .empNo("209901002")
            .build();
    }

    private Users user(UserRole role) {
        return Users.builder()
            .id(3L)
            .name("요청자")
            .role(role)
            .empNo("209901003")
            .build();
    }

    private Items item(Long id) {
        return Items.builder()
            .id(id)
            .itemName("아이템")
            .itemCode("ITEM001")
            .build();
    }

    private Items item() {
        return Items.builder()
            .id(1L)
            .itemName("아이템")
            .itemCode("ITEM001")
            .build();
    }

    private ItemsLines itemLine() {
        Lines lines = line();
        Items items = item();
        return ItemsLines.builder()
            .id(1L)
            .line(lines)
            .lineId(lines.getId())
            .item(items)
            .itemId(items.getId())
            .build();
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

    private Equipments equipment() {
        return Equipments.builder()
            .equipmentPpm(BigDecimal.valueOf(1000))
            .totalCount(BigDecimal.valueOf(1000))
            .defectiveCount(BigDecimal.valueOf(10))
            .build();
    }

    private ProductionPlans plan(Long id) {
        return ProductionPlans.builder()
            .id(id)
            .status(PlanStatus.PENDING)
            .startTime(LocalDateTime.of(2099, 1, 1, 9, 0))
            .endTime(LocalDateTime.of(2099, 1, 1, 10, 0))
            .createdAt(LocalDateTime.now(fixedClock))
            .build();
    }

    @Nested
    @DisplayName("생산계획 생성")
    class createProductionPlanTest {
        private LocalDate testDate;
        private LocalDateTime testDateTime;

        private ItemsLines itemsLines;
        private Lines line;
        private Items item;
        private Factories factory;
        private Equipments equipment;
        private Users salesManager;
        private Users productionManager;
        private Users requestUser;

        @BeforeEach
        void setUp() {
            itemsLines = itemLine();
            line = itemsLines.getLine();
            item = itemsLines.getItem();
            factory = line.getFactory();
            equipment = equipment();
            salesManager = salesManager();
            productionManager = productionManager();
            requestUser = user(UserRole.USER);
            testDate = LocalDate.now(fixedClock);
            testDateTime = LocalDateTime.now(fixedClock);
        }

        private CreateProductionPlanRequestDto createRequestDto() {
            return CreateProductionPlanRequestDto.builder()
                .dueDate(testDate)
                .salesManagerNo(salesManager.getEmpNo())
                .productionManagerNo(productionManager.getEmpNo())
                .factoryCode(factory.getFactoryCode())
                .itemCode(item.getItemCode())
                .plannedQty(BigDecimal.valueOf(100))
                .lineCode(line.getLineCode())
                .remark("테스트 생산 계획")
                .build();
        }

        @Test
        @DisplayName("유저 권한으로 생산 계획을 성공적으로 등록합니다.")
        void createProductionPlan_WhenUser_success() {
            // given
            CreateProductionPlanRequestDto requestDto = createRequestDto();

            when(userRepository.findByEmpNo(salesManager.getEmpNo())).thenReturn(Optional.of(salesManager));
            when(userRepository.findByEmpNo(productionManager.getEmpNo())).thenReturn(Optional.of(productionManager));
            when(lineRepository.findBylineCode(line.getLineCode())).thenReturn(Optional.of(line));
            when(factoryRepository.findByFactoryCode(factory.getFactoryCode())).thenReturn(Optional.of(factory));
            when(itemRepository.findByItemCode(item.getItemCode())).thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemId(line.getId(), item.getId()))
                .thenReturn(Optional.of(itemsLines));
            when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(List.of(equipment));
            when(productionPlanRepository.findByLineCodeAndStatusInAndEndTimeAfterOrderByCreatedAtDesc(
                eq(line.getLineCode()), anyList(), any(LocalDateTime.class)
            )).thenReturn(Optional.empty());
            when(productionPlanRepository.findByDocumentNoByPrefix(anyString())).thenReturn(List.of());

            when(productionPlanRepository.save(any(ProductionPlans.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            GetProductionPlanResponseDto responseDto = productionPlanService.createProductionPlan(requestDto, requestUser);

            // then
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.getDocumentNo()).isEqualTo("2099/01/01-1");
            assertThat(responseDto.getLineCode()).isEqualTo(line.getLineCode());
            assertThat(responseDto.getPlannedQty()).isEqualTo(BigDecimal.valueOf(100));
            assertThat(responseDto.getStatus()).isEqualTo(PlanStatus.PENDING);
            assertThat(responseDto.getStartTime()).isAfterOrEqualTo(testDateTime);
            assertThat(responseDto.getEndTime()).isAfter(responseDto.getStartTime());

            verify(productionPlanRepository, times(1)).save(any(ProductionPlans.class));
            verify(equipmentRepository, times(1)).findAllByLineId(line.getId());
        }

        @Test
        @DisplayName("관리자 권한이면 PlanStatus가 CONFIRMED로 설정")
        void createProductionPlan_admin_setsConfirmedStatus() {

            CreateProductionPlanRequestDto dto = createRequestDto();
            Users adminUser = requestUser.toBuilder()
                .role(UserRole.ADMIN)
                .build();

            when(userRepository.findByEmpNo(dto.getSalesManagerNo())).thenReturn(Optional.of(salesManager()));
            when(userRepository.findByEmpNo(dto.getProductionManagerNo())).thenReturn(Optional.of(productionManager()));
            when(lineRepository.findBylineCode(line.getLineCode())).thenReturn(Optional.of(line));
            when(factoryRepository.findByFactoryCode(dto.getFactoryCode())).thenReturn(Optional.of(line.getFactory()));
            when(itemRepository.findByItemCode(item.getItemCode())).thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemId(line.getId(), item.getId())).thenReturn(Optional.of(itemsLines));
            when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(List.of(equipment));
            when(productionPlanRepository.findByLineCodeAndStatusInAndEndTimeAfterOrderByCreatedAtDesc(
                anyString(), anyList(), any(LocalDateTime.class))).thenReturn(Optional.empty());
            when(productionPlanRepository.findByDocumentNoByPrefix(anyString())).thenReturn(List.of());
            when(productionPlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            GetProductionPlanResponseDto response = productionPlanService.createProductionPlan(dto, adminUser);

            assertThat(response.getStatus()).isEqualTo(PlanStatus.CONFIRMED);
        }

        @Test
        @DisplayName("관리자 권한으로 CONFIRMED 생산 계획 생성 시 시작 시간이 PENDING보다 짧게 설정")
        void createProductionPlan_WhenAdminConfirmed_StartTimeShorter() {
            // given
            LocalDateTime startTime = testDateTime;

            CreateProductionPlanRequestDto dto = createRequestDto();

            Users admin = requestUser.toBuilder()
                .role(UserRole.ADMIN)
                .build();

            // repository mocking
            when(userRepository.findByEmpNo(dto.getSalesManagerNo())).thenReturn(Optional.of(salesManager()));
            when(userRepository.findByEmpNo(dto.getProductionManagerNo())).thenReturn(Optional.of(productionManager()));
            when(lineRepository.findBylineCode(line.getLineCode())).thenReturn(Optional.of(line));
            when(factoryRepository.findByFactoryCode(dto.getFactoryCode())).thenReturn(Optional.of(line.getFactory()));
            when(itemRepository.findByItemCode(item.getItemCode())).thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemId(line.getId(), item.getId())).thenReturn(Optional.of(itemsLines));
            when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(List.of(equipment));
            when(productionPlanRepository.findByLineCodeAndStatusInAndEndTimeAfterOrderByCreatedAtDesc(
                anyString(), anyList(), any(LocalDateTime.class))).thenReturn(Optional.empty());
            when(productionPlanRepository.findByDocumentNoByPrefix(anyString())).thenReturn(List.of());
            when(productionPlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            GetProductionPlanResponseDto responseDto = productionPlanService.createProductionPlan(dto, admin);

            // then
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.getStartTime()).isAfterOrEqualTo(startTime.plusMinutes(10)); // CONFIRMED 권한이라 PENDING보다 짧음
            assertThat(responseDto.getEndTime()).isAfter(responseDto.getStartTime());

            verify(productionPlanRepository, times(1)).save(any(ProductionPlans.class));
            verify(equipmentRepository, times(1)).findAllByLineId(line.getId());
        }

        @Test
        @DisplayName("SalesManager가 존재하지 않으면 예외 발생")
        void createProductionPlan_salesManagerNotFound_throws() {
            // given
            CreateProductionPlanRequestDto dto = createRequestDto();

            when(userRepository.findByEmpNo(dto.getSalesManagerNo())).thenReturn(Optional.empty());

            // when and then
            assertThatThrownBy(() -> productionPlanService.createProductionPlan(dto, requestUser))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("ProductionManager가 존재하지 않으면 예외 발생")
        void createProductionPlan_productionManagerNotFound_throws() {
            CreateProductionPlanRequestDto dto = createRequestDto();

            when(userRepository.findByEmpNo(dto.getSalesManagerNo())).thenReturn(Optional.of(salesManager()));
            when(userRepository.findByEmpNo(dto.getProductionManagerNo())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productionPlanService.createProductionPlan(dto, requestUser))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("라인이 존재하지 않으면 예외 발생")
        void createProductionPlan_lineNotFound_throws() {
            CreateProductionPlanRequestDto dto = createRequestDto();

            when(userRepository.findByEmpNo(dto.getSalesManagerNo())).thenReturn(Optional.of(salesManager()));
            when(userRepository.findByEmpNo(dto.getProductionManagerNo())).thenReturn(Optional.of(productionManager()));
            when(lineRepository.findBylineCode(dto.getLineCode())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productionPlanService.createProductionPlan(dto, requestUser))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(LineErrorCode.LINE_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("라인에 설비가 없으면 예외 발생")
        void createProductionPlan_noEquipment_throws() {
            CreateProductionPlanRequestDto dto = createRequestDto();

            when(userRepository.findByEmpNo(dto.getSalesManagerNo())).thenReturn(Optional.of(salesManager()));
            when(userRepository.findByEmpNo(dto.getProductionManagerNo())).thenReturn(Optional.of(productionManager()));
            when(lineRepository.findBylineCode(line.getLineCode())).thenReturn(Optional.of(line));
            when(factoryRepository.findByFactoryCode(dto.getFactoryCode())).thenReturn(Optional.of(line.getFactory()));
            when(itemRepository.findByItemCode(item.getItemCode())).thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemId(line.getId(), item.getId())).thenReturn(Optional.of(itemsLines));
            when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> productionPlanService.createProductionPlan(dto, requestUser))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(LineErrorCode.NO_EQUIPMENT_FOUND.getMessage());
        }

        @Test
        @DisplayName("PPM이 0 이하인 설비면 예외 발생")
        void createProductionPlan_invalidPPM_throws() {

            // given
            Equipments ppmZeroEquipment = Equipments.builder()
                .equipmentPpm(BigDecimal.ZERO)
                .totalCount(BigDecimal.ONE)
                .defectiveCount(BigDecimal.ZERO)
                .build();

            CreateProductionPlanRequestDto dto = createRequestDto();

            when(userRepository.findByEmpNo(dto.getSalesManagerNo())).thenReturn(Optional.of(salesManager()));
            when(userRepository.findByEmpNo(dto.getProductionManagerNo())).thenReturn(Optional.of(productionManager()));
            when(lineRepository.findBylineCode(line.getLineCode())).thenReturn(Optional.of(line));
            when(factoryRepository.findByFactoryCode(dto.getFactoryCode())).thenReturn(Optional.of(line.getFactory()));
            when(itemRepository.findByItemCode(item.getItemCode())).thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemId(line.getId(), item.getId())).thenReturn(Optional.of(itemsLines));
            when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(List.of(ppmZeroEquipment));

            assertThatThrownBy(() -> productionPlanService.createProductionPlan(dto, requestUser))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(LineErrorCode.INVALID_EQUIPMENT_PPM.getMessage());
        }

        @Test
        @DisplayName("최근 생산계획이 존재하면 시작시간이 최근 종료시간 + 30분으로 설정")
        void createProductionPlan_WhenExistingPlanExists_startTimeAfterPrevious() {
            // given
            LocalDateTime existingEndTime = LocalDateTime.now(fixedClock).plusHours(2);
            ProductionPlans existingPlan = ProductionPlans.builder()
                .documentNo("2099/01/01-1")
                .endTime(existingEndTime)
                .status(PlanStatus.CONFIRMED)
                .build();

            CreateProductionPlanRequestDto requestDto = createRequestDto();

            when(userRepository.findByEmpNo(salesManager.getEmpNo())).thenReturn(Optional.of(salesManager));
            when(userRepository.findByEmpNo(productionManager.getEmpNo())).thenReturn(Optional.of(productionManager));
            when(lineRepository.findBylineCode(line.getLineCode())).thenReturn(Optional.of(line));
            when(factoryRepository.findByFactoryCode(factory.getFactoryCode())).thenReturn(Optional.of(factory));
            when(itemRepository.findByItemCode(item.getItemCode())).thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemId(line.getId(), item.getId()))
                .thenReturn(Optional.of(itemsLines));
            when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(List.of(equipment));
            when(productionPlanRepository.findByLineCodeAndStatusInAndEndTimeAfterOrderByCreatedAtDesc(
                eq(line.getLineCode()), anyList(), any(LocalDateTime.class)
            )).thenReturn(Optional.of(existingPlan));
            when(productionPlanRepository.findByDocumentNoByPrefix(anyString())).thenReturn(List.of(existingPlan.getDocumentNo()));

            when(productionPlanRepository.save(any(ProductionPlans.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            GetProductionPlanResponseDto result = productionPlanService.createProductionPlan(requestDto, requestUser);

            // then
            assertThat(result.getStartTime()).isEqualTo(existingEndTime.plusMinutes(30));
            assertThat(result.getDocumentNo()).isEqualTo("2099/01/01-2");
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
        GetProductionPlanDetailResponseDto response =
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
        SearchProductionPlanCommand command = SearchProductionPlanCommand.builder()
            .status(PlanStatus.PENDING)
            .factoryName("1공장")
            .salesManagerName("유저")
            .productionManagerName("유저")
            .itemName("아이템")
            .dueDate(LocalDate.now(fixedClock))
            .startTime(LocalDateTime.now(fixedClock).minusDays(1))
            .endTime(LocalDateTime.now(fixedClock).plusDays(1))
            .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "documentNo"));

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
        Page<GetProductionPlanListResponseDto> result =
            productionPlanService.getProductionPlanList(command, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        GetProductionPlanListResponseDto dto = result.getContent().getFirst();

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
    @DisplayName("생산 계획 목록 조회 - 계획 수량 오름차순 확인")
    void getProductionPlanList_clientSortMerged() {
        // given
        SearchProductionPlanCommand command = SearchProductionPlanCommand.builder()
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
        Page<GetProductionPlanListResponseDto> result =
            productionPlanService.getProductionPlanList(command, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        GetProductionPlanListResponseDto dto = result.getContent().getFirst();
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
        SearchProductionPlanCommand command = SearchProductionPlanCommand.builder().build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProductionPlans> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(productionPlanRepository.findAll(ArgumentMatchers.<Specification<ProductionPlans>>any(), any(Pageable.class)))
            .thenReturn(emptyPage);

        // when
        Page<GetProductionPlanListResponseDto> result =
            productionPlanService.getProductionPlanList(command, pageable);

        // then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    private UpdateProductionPlanRequestDto dto() {
        return UpdateProductionPlanRequestDto.builder()
            .status(PlanStatus.PENDING)
            .salesManagerNo("S001")
            .productionManagerNo("P001")
            .lineCode("L001")
            .factoryCode("F001")
            .itemCode("ITEM001")
            .endTime(LocalDateTime.of(2099, 1, 1, 11, 0))
            .build();
    }

    @Test
    @DisplayName("생산계획 수정 성공 - 일반 담당자")
    void update_success_user() {
        // given
        ProductionPlans plan = plan(1L);
        UpdateProductionPlanRequestDto request = dto();

        Users requestUser = user("T001");
        Users sManager = Users.builder().role(UserRole.MANAGER).empNo("S001").build();
        Users pManager = Users.builder().role(UserRole.MANAGER).empNo("P001").build();
        Lines lines = line(1L);
        Items items = item(1L);

        when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(userRepository.findByEmpNo("S001")).thenReturn(Optional.of(sManager));
        when(userRepository.findByEmpNo("P001")).thenReturn(Optional.of(pManager));
        when(lineRepository.findBylineCode("L001")).thenReturn(Optional.of(lines));
        when(factoryRepository.findByFactoryCode("F001")).thenReturn(Optional.of(factory()));
        when(itemRepository.findByItemCode("ITEM001")).thenReturn(Optional.of(items));
        when(itemLineRepository.findByLineIdAndItemId(1L, 1L)).thenReturn(Optional.of(itemLine(lines, items)));

        when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(any(), anyList())).thenReturn(List.of());

        // when
        GetProductionPlanResponseDto response =
            productionPlanService.updateProductionPlan(request, 1L, requestUser);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PlanStatus.PENDING);

        verify(productionPlanRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("생산계획 수정 실패 - 권한 오류 : 관리자가 PENDING/CONFIRMED 외 상태로 변경 시 실패")
    void update_fail_manager_status_forbidden() {
        // given
        ProductionPlans plan = plan(1L);

        UpdateProductionPlanRequestDto request = UpdateProductionPlanRequestDto.builder()
            .status(PlanStatus.RUNNING) // 허용되지 않음
            .build();
        Users requestUser = user("S001");

        when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() ->
            productionPlanService.updateProductionPlan(request, 1L, requestUser))
            .isInstanceOf(AppException.class)
            .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN.getMessage());
    }

    @Test
    @DisplayName("생산계획 수정 실패 - 계획 상태가 PENDING/CONFIRMED이 아니면 수정 불가")
    void update_fail_plan_not_updatable() {
        ProductionPlans plan = plan(1L);
        plan.updateStatus(PlanStatus.RUNNING); // isUpdatable() = false

        when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        UpdateProductionPlanRequestDto requestDto = dto();
        Users requestUser = user("S001");

        assertThatThrownBy(() ->
            productionPlanService.updateProductionPlan(requestDto, 1L, requestUser))
            .isInstanceOf(AppException.class)
            .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_BAD_REQUEST.getMessage());
    }

    // ============================================================================================
    // 4. 라인/아이템/팩토리 검증 실패
    // ============================================================================================

    @Test
    @DisplayName("생산계획 수정 실패 - 아이템-라인 조합이 없으면 실패")
    void update_fail_itemLine_not_found() {
        ProductionPlans plan = plan(1L);

        Users requestUser = user("T001");
        Users sManager = Users.builder().role(UserRole.MANAGER).empNo("S001").build();
        Users pManager = Users.builder().role(UserRole.MANAGER).empNo("P001").build();

        when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(lineRepository.findBylineCode("L001")).thenReturn(Optional.of(line(1L)));
        when(factoryRepository.findByFactoryCode("F001")).thenReturn(Optional.of(factory()));
        when(itemRepository.findByItemCode("ITEM001")).thenReturn(Optional.of(item(1L)));
        when(userRepository.findByEmpNo("S001")).thenReturn(Optional.of(sManager));
        when(userRepository.findByEmpNo("P001")).thenReturn(Optional.of(pManager));

        when(itemLineRepository.findByLineIdAndItemId(1L, 1L)).thenReturn(Optional.empty());

        UpdateProductionPlanRequestDto requestDto = dto();

        assertThatThrownBy(() ->
            productionPlanService.updateProductionPlan(requestDto, 1L, requestUser)
        )
            .isInstanceOf(AppException.class)
            .hasMessageContaining(ItemLineErrorCode.ITEM_LINE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("생산계획 수정 실패 - 공장-라인 매칭 오류 시 실패")
    void update_fail_line_not_in_factory() {
        ProductionPlans plan = plan(1L);

        Lines wrongLine = Lines.builder()
            .id(1L)
            .factoryId(999L) // 다른 공장
            .factory(Factories.builder().id(999L).build())
            .build();
        Items items = item(1L);
        Factories factories = factory();
        Users sManager = Users.builder().role(UserRole.MANAGER).empNo("S001").build();
        Users pManager = Users.builder().role(UserRole.MANAGER).empNo("P001").build();

        when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(lineRepository.findBylineCode("L001")).thenReturn(Optional.of(wrongLine));
        when(factoryRepository.findByFactoryCode("F001")).thenReturn(Optional.of(factories));
        when(itemRepository.findByItemCode("ITEM001")).thenReturn(Optional.of(items));
        when(itemLineRepository.findByLineIdAndItemId(any(), any()))
            .thenReturn(Optional.of(itemLine(wrongLine, items)));
        when(userRepository.findByEmpNo("S001")).thenReturn(Optional.of(sManager));
        when(userRepository.findByEmpNo("P001")).thenReturn(Optional.of(pManager));

        UpdateProductionPlanRequestDto requestDto = dto();
        Users requestUser = user("S001");

        assertThatThrownBy(() ->
            productionPlanService.updateProductionPlan(requestDto, 1L, requestUser)
        )
            .isInstanceOf(AppException.class)
            .hasMessageContaining(LineErrorCode.LINE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("생산계획 수정 성공 - 이후 계획 정상 이동 - 일반 사용자")
    void update_success_shift_user() {
        ProductionPlans plan = plan(1L);

        ProductionPlans after = plan(2L);
        Lines lines = line(1L);
        Items items = item(1L);

        when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(userRepository.findByEmpNo(anyString())).thenReturn(Optional.of(user("S001")));
        when(lineRepository.findBylineCode(any())).thenReturn(Optional.of(lines));
        when(factoryRepository.findByFactoryCode(any())).thenReturn(Optional.of(factory()));
        when(itemRepository.findByItemCode(any())).thenReturn(Optional.of(items));
        when(itemLineRepository.findByLineIdAndItemId(any(), any()))
            .thenReturn(Optional.of(itemLine(lines, items)));

        when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(any(), anyList()))
            .thenReturn(List.of(after));

        GetProductionPlanResponseDto response =
            productionPlanService.updateProductionPlan(dto(), 1L, user("M001"));

        assertThat(response).isNotNull();
        verify(productionPlanRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("생산계획 수정 실패 - 생산계획 ID 조회 실패 시 예외")
    void update_fail_plan_not_found() {
        when(productionPlanRepository.findById(anyLong())).thenReturn(Optional.empty());

        UpdateProductionPlanRequestDto requestDto = dto();
        Users requestUser = user("S001");

        assertThatThrownBy(() ->
            productionPlanService.updateProductionPlan(requestDto, 1L, requestUser)
        ).isInstanceOf(AppException.class)
            .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND.getMessage());
    }

    // ============================================================================================
    // 5. 이후 계획 이동
    // ============================================================================================

    @Nested
    @DisplayName("updateProductionPlan – 생산계획 이동에 의한 수정 테스트")
    class UpdateProductionPlan {

        private Users manager;
        private ItemsLines itemLine;
        private Items item;
        private Lines line;
        private Factories factory;


        @BeforeEach
        void mockCommonDependencies() {
            manager = Users.builder().role(UserRole.MANAGER).build();

            factory = Factories.builder().id(1L).factoryCode("F1").build();
            line = Lines.builder().id(10L).lineCode("L1").factoryId(1L).build();
            item = Items.builder().id(20L).itemCode("ITEM1").build();
            itemLine = ItemsLines.builder().id(30L).lineId(10L).line(line).item(item).itemId(20L).build();

            // 모든 공통 레포 mock
            lenient().when(userRepository.findByEmpNo(any())).thenReturn(Optional.of(manager));
            lenient().when(lineRepository.findBylineCode(any())).thenReturn(Optional.of(line));
            lenient().when(itemRepository.findByItemCode(any())).thenReturn(Optional.of(item));
            lenient().when(factoryRepository.findByFactoryCode(any())).thenReturn(Optional.of(factory));
            lenient().when(itemLineRepository.findByLineIdAndItemId(any(), any())).thenReturn(Optional.of(itemLine));
        }

        private ProductionPlans plan(Long id, String start, String end, PlanStatus status) {
            return ProductionPlans.builder()
                .id(id)
                .itemLine(itemLine)
                .salesManager(manager)
                .productionManager(manager)
                .startTime(time(start))
                .endTime(time(end))
                .status(status)
                .build();
        }

        private LocalDateTime time(String localTime) {
            return LocalDateTime.of(LocalDate.now(fixedClock), LocalTime.parse(localTime));
        }

        private UpdateProductionPlanRequestDto updateDto(String start, String end) {
            return UpdateProductionPlanRequestDto.builder()
                .startTime(time(start))
                .endTime(time(end))
                .status(PlanStatus.PENDING)
                .factoryCode("F1")
                .lineCode("L1")
                .itemCode("ITEM1")
                .build();
        }

        /*
        Case: A(수정 대상)
              A: 09:00~10:00 → 새로 09:00~10:30 로 변경
              B: 10:15~11:00 → A와 겹침 → delta 30분 적용 → 11:00~11:45 로 변경
         */
        @Test
        @DisplayName("겹치는 계획이 있으면 뒤로 밀린다")
        void shift_should_push_overlapped_plans() {
            // given
            ProductionPlans ppA = plan(1L, "09:00", "10:00", PlanStatus.PENDING);
            ProductionPlans ppB = plan(2L, "10:15", "11:00", PlanStatus.PENDING);

            UpdateProductionPlanRequestDto dto = updateDto("09:00", "10:30");

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED))
            ).thenReturn(List.of(ppA, ppB));

            // when
            productionPlanService.updateProductionPlan(dto, 1L, manager);

            // then
            assertThat(ppB.getStartTime()).isEqualTo(time("11:00"));
            assertThat(ppB.getEndTime()).isEqualTo(time("11:45"));
        }

        /*
         A: 09:00~10:00 → 수정으로 endTime = 10:00 유지
         B: 10:20~11:00 (gap = 20분 → delta 30보다 작음)
         → B는 10:30~11:10 로 이동
         */
        @Test
        @DisplayName("겹치지 않아도 delta 30분 간격보다 부족하면 보정된다")
        void shift_gap_less_than_delta() {
            ProductionPlans ppA = plan(1L, "09:00", "10:00", PlanStatus.PENDING);
            ProductionPlans ppB = plan(2L, "10:20", "11:00", PlanStatus.PENDING);

            UpdateProductionPlanRequestDto dto = updateDto("09:00", "10:00");

            when(productionPlanRepository.findById(1L))
                .thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED)
            )).thenReturn(List.of(ppA, ppB));

            productionPlanService.updateProductionPlan(dto, 1L, manager);

            assertThat(ppB.getStartTime()).isEqualTo(time("10:30"));
            assertThat(ppB.getEndTime()).isEqualTo(time("11:10"));
        }

        /*
         A 수정됨
         B (CONFIRMED)이 뒤로 밀려야 하는 상황 → 예외 발생
         */
        @Test
        @DisplayName("재배치 도중 CONFIRMED를 침해하면 예외가 발생한다")
        void shift_should_throw_if_confirmed_plan_is_affected() {
            ProductionPlans ppA = plan(1L, "09:00", "10:00", PlanStatus.PENDING);
            ProductionPlans ppB = plan(2L, "10:00", "11:00", PlanStatus.CONFIRMED);

            UpdateProductionPlanRequestDto dto = updateDto("09:00", "10:30");

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED))
            ).thenReturn(List.of(ppA, ppB));

            assertThatThrownBy(() ->
                productionPlanService.updateProductionPlan(dto, 1L, manager)
            ).isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN.getMessage());
        }

        @Test
        @DisplayName("자기 자신(newPlan)은 afterPlans 리스트에서 제거된다")
        void shift_should_remove_itself_from_afterPlans() {
            ProductionPlans ppA = plan(1L, "09:00", "10:00", PlanStatus.PENDING);
            ProductionPlans ppB = plan(2L, "10:30", "11:00", PlanStatus.PENDING);

            UpdateProductionPlanRequestDto dto = updateDto("09:00", "10:00");

            when(productionPlanRepository.findById(1L))
                .thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED)
            )).thenReturn(List.of(ppA, ppB));

            productionPlanService.updateProductionPlan(dto, 1L, manager);

            verify(productionPlanRepository).saveAll(
                argThat(iter -> {
                    List<ProductionPlans> list = StreamSupport
                        .stream(iter.spliterator(), false)
                        .toList();

                    return list.size() == 1 &&
                        list.getFirst().getId().equals(2L);
                })
            );
        }

        @Test
        @DisplayName("충돌도 없고 delta 조건도 넉넉하면 이동하지 않는다")
        void shift_no_move_when_no_overlap_and_gap_ok() {
            ProductionPlans ppA = plan(1L, "09:00", "10:00", PlanStatus.PENDING);
            ProductionPlans ppB = plan(2L, "10:40", "11:00", PlanStatus.PENDING);

            UpdateProductionPlanRequestDto dto = updateDto("09:00", "10:00");

            when(productionPlanRepository.findById(1L))
                .thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED)
            )).thenReturn(List.of(ppA, ppB));

            productionPlanService.updateProductionPlan(dto, 1L, manager);

            assertThat(ppB.getStartTime()).isEqualTo(time("10:40"));
            assertThat(ppB.getEndTime()).isEqualTo(time("11:00"));
        }

    }
}