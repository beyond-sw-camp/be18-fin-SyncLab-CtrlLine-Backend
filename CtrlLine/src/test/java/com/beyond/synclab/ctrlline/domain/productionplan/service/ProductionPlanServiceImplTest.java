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

    private Clock testClock;

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
    private ProductionPlans productionPlan;

    @BeforeEach
    void setUp() {
        // 고정된 시간
        testClock = Clock.fixed(
            Instant.parse("2099-01-01T00:00:00Z"),
            ZoneId.systemDefault()
        );
        testDate = LocalDate.now(testClock);
        testDateTime = LocalDateTime.now(testClock);

        // 서비스에 Mock + FixedClock 직접 주입
        productionPlanService = new ProductionPlanServiceImpl(
            productionPlanRepository,
            userRepository,
            lineRepository,
            factoryRepository,
            itemLineRepository,
            itemRepository,
            equipmentRepository,
            testClock
        );

        factory = Factories.builder()
            .id(1L)
            .factoryCode("F001")
            .build();

        line = Lines.builder()
            .id(1L)
            .lineName("라인")
            .lineCode("LINE01")
            .factoryId(factory.getId())
            .factory(factory)
            .build();

        item = Items.builder()
            .id(1L)
            .itemName("아이템")
            .itemCode("ITEM001")
            .build();

        itemsLines = ItemsLines.builder()
            .id(1L)
            .line(line)
            .lineId(line.getId())
            .item(item)
            .itemId(item.getId())
            .build();

        equipment = Equipments.builder()
            .equipmentPpm(BigDecimal.valueOf(1000))
            .totalCount(BigDecimal.valueOf(1000))
            .defectiveCount(BigDecimal.valueOf(10))
            .build();

        salesManager = Users.builder()
            .id(1L)
            .name("영업담당자1")
            .role(UserRole.MANAGER)
            .empNo("209901001")
            .build();

        productionManager = Users.builder()
            .id(2L)
            .name("생산담당자1")
            .role(UserRole.MANAGER)
            .empNo("209901002")
            .build();

        requestUser = Users.builder()
            .id(3L)
            .name("요청자")
            .role(UserRole.USER)
            .empNo("209901003")
            .build();

        productionPlan = ProductionPlans.builder()
            .id(1L)
            .documentNo("2099/01/01-1")
            .salesManagerId(salesManager.getId())
            .salesManager(salesManager)
            .productionManager(productionManager)
            .productionManagerId(productionManager.getId())
            .itemLineId(itemsLines.getId())
            .itemLine(itemsLines)
            .plannedQty(BigDecimal.valueOf(100))
            .status(PlanStatus.PENDING)
            .dueDate(testDate)
            .startTime(testDateTime)
            .endTime(testDateTime.plusDays(1))
            .createdAt(testDateTime)
            .remark("testRemark")
            .build();
    }

    @Nested
    @DisplayName("생산계획생성 테스트")
    class createProductionPlanTest {

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

            when(userRepository.findByEmpNo(dto.getSalesManagerNo())).thenReturn(Optional.of(salesManager));
            when(userRepository.findByEmpNo(dto.getProductionManagerNo())).thenReturn(Optional.of(productionManager));
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
            when(userRepository.findByEmpNo(dto.getSalesManagerNo())).thenReturn(Optional.of(salesManager));
            when(userRepository.findByEmpNo(dto.getProductionManagerNo())).thenReturn(Optional.of(productionManager));
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

            when(userRepository.findByEmpNo(dto.getSalesManagerNo())).thenReturn(Optional.of(salesManager));
            when(userRepository.findByEmpNo(dto.getProductionManagerNo())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productionPlanService.createProductionPlan(dto, requestUser))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("라인이 존재하지 않으면 예외 발생")
        void createProductionPlan_lineNotFound_throws() {
            CreateProductionPlanRequestDto dto = createRequestDto();

            when(userRepository.findByEmpNo(dto.getSalesManagerNo())).thenReturn(Optional.of(salesManager));
            when(userRepository.findByEmpNo(dto.getProductionManagerNo())).thenReturn(Optional.of(productionManager));
            when(lineRepository.findBylineCode(dto.getLineCode())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productionPlanService.createProductionPlan(dto, requestUser))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(LineErrorCode.LINE_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("라인에 설비가 없으면 예외 발생")
        void createProductionPlan_noEquipment_throws() {
            CreateProductionPlanRequestDto dto = createRequestDto();

            when(userRepository.findByEmpNo(dto.getSalesManagerNo())).thenReturn(Optional.of(salesManager));
            when(userRepository.findByEmpNo(dto.getProductionManagerNo())).thenReturn(Optional.of(productionManager));
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

            when(userRepository.findByEmpNo(dto.getSalesManagerNo())).thenReturn(Optional.of(salesManager));
            when(userRepository.findByEmpNo(dto.getProductionManagerNo())).thenReturn(Optional.of(productionManager));
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
            LocalDateTime existingEndTime = LocalDateTime.now(testClock).plusHours(2);
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

    @Nested
    @DisplayName("생산계획 상세 조회")
    class getProductionPlanTest {
        @Test
        @DisplayName("생산 계획 상세 조회에 성공한다.")
        void getProductionPlan_success() {
            // given
            Long planId = 1L;

            ProductionPlans productionPlans = ProductionPlans.builder()
                .id(planId)
                .salesManager(salesManager)
                .productionManager(productionManager)
                .itemLine(itemsLines)
                .documentNo("2099/01/01-1")
                .plannedQty(BigDecimal.valueOf(500))
                .build();

            when(productionPlanRepository.findById(planId))
                .thenReturn(Optional.of(productionPlans));

            // when
            GetProductionPlanDetailResponseDto response = productionPlanService.getProductionPlan(planId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getPlanDocumentNo()).isEqualTo("2099/01/01-1");
            assertThat(response.getItemCode()).isEqualTo(item.getItemCode());
            assertThat(response.getFactoryCode()).isEqualTo(factory.getFactoryCode());

            verify(productionPlanRepository, times(1)).findById(planId);
        }

        @Test
        @DisplayName("생산 계획 ID가 존재하지 않을 경우 예외를 반환한다.")
        void getProductionPlan_notFound() {
            // given
            Long planId = 999L;

            when(productionPlanRepository.findById(planId)).thenReturn(Optional.empty());

            // expected
            assertThatThrownBy(() -> productionPlanService.getProductionPlan(planId))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND.getMessage());

            verify(productionPlanRepository, times(1)).findById(planId);
        }
    }


    @Nested
    @DisplayName("생산계획 목록조회")
    class getProductionPlanListTest {

        @Test
        @DisplayName("파라미터를 정상 적용하여 조회 성공")
        void getProductionPlanList_success() {
            // given
            SearchProductionPlanCommand command = SearchProductionPlanCommand.builder()
                .status(PlanStatus.PENDING)
                .factoryName(factory.getFactoryName())
                .salesManagerName(salesManager.getName())
                .productionManagerName(productionManager.getName())
                .itemName(item.getItemName())
                .dueDate(testDate)
                .startTime(testDateTime.minusDays(1))
                .endTime(testDateTime.plusDays(1))
                .build();

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "documentNo"));

            ProductionPlans planA = productionPlan.toBuilder()
                .status(PlanStatus.PENDING)
                .build();

            Page<ProductionPlans> mockPage = new PageImpl<>(List.of(planA), pageable, 1);

            when(productionPlanRepository.findAll(ArgumentMatchers.<Specification<ProductionPlans>>any(), any(Pageable.class)))
                .thenReturn(mockPage);

            // when
            Page<GetProductionPlanListResponseDto> result =
                productionPlanService.getProductionPlanList(command, pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            GetProductionPlanListResponseDto dto = result.getContent().getFirst();

            assertThat(dto.getId()).isEqualTo(planA.getId());
            assertThat(dto.getDocumentNo()).isEqualTo(planA.getDocumentNo());
            assertThat(dto.getStatus()).isEqualTo(planA.getStatus());
            assertThat(dto.getPlannedQty()).isEqualTo(planA.getPlannedQty());
            assertThat(dto.getDueDate()).isEqualTo(planA.getDueDate());
            assertThat(dto.getRemark()).isEqualTo(planA.getRemark());

            // repository 호출 검증
            verify(productionPlanRepository, times(1))
                .findAll(ArgumentMatchers.<Specification<ProductionPlans>>any(), any(Pageable.class));
        }

        @Test
        @DisplayName("계획 수량 오름차순 확인")
        void getProductionPlanList_clientSortMerged() {
            // given
            SearchProductionPlanCommand command = SearchProductionPlanCommand.builder()
                .status(PlanStatus.PENDING)
                .build();

            // 클라이언트가 name ASC 정렬 요청
            Pageable pageable = PageRequest.of(0, 10, Sort.by("plannedQty").ascending());

            ProductionPlans planA = productionPlan.toBuilder()
                .documentNo("2099/01/01-1")
                .plannedQty(BigDecimal.valueOf(400))
                .build();

            ProductionPlans planB = productionPlan.toBuilder()
                .documentNo("2099/01/01-2")
                .plannedQty(BigDecimal.valueOf(300))
                .build();

            Page<ProductionPlans> mockPage = new PageImpl<>(List.of(planA, planB), pageable, 2);
            when(productionPlanRepository.findAll(ArgumentMatchers.<Specification<ProductionPlans>>any(), any(Pageable.class)))
                .thenReturn(mockPage);

            // when
            Page<GetProductionPlanListResponseDto> result =
                productionPlanService.getProductionPlanList(command, pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().getFirst().getId()).isEqualTo(planA.getId());

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
        @DisplayName("빈 데이터 조회")
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
    }

    @Nested
    @DisplayName("생산계획 수정 테스트")
    class updateProductionPlanTest {
        @BeforeEach
        void setUp() {
            requestUser = requestUser.toBuilder()
                .role(UserRole.MANAGER)
                .build();
        }

        private UpdateProductionPlanRequestDto createUpdateRequestDto() {
            return UpdateProductionPlanRequestDto.builder()
                .status(PlanStatus.PENDING)
                .salesManagerNo(salesManager.getEmpNo())
                .productionManagerNo(productionManager.getEmpNo())
                .lineCode(line.getLineCode())
                .factoryCode(factory.getFactoryCode())
                .itemCode(item.getItemCode())
                .plannedQty(BigDecimal.valueOf(500))
                .startTime(testDateTime.plusDays(1))
                .remark("testRemark")
                .build();
        }

        @Test
        @DisplayName("성공 - MANAGER 권한 유저 수정, 차후 계획 없음")
        void update_success_user() {
            // given
            UpdateProductionPlanRequestDto request = createUpdateRequestDto();

            when(productionPlanRepository.findById(productionPlan.getId())).thenReturn(Optional.of(productionPlan));
            when(userRepository.findByEmpNo(salesManager.getEmpNo())).thenReturn(Optional.of(salesManager));
            when(userRepository.findByEmpNo(productionManager.getEmpNo())).thenReturn(Optional.of(productionManager));
            when(lineRepository.findBylineCode(line.getLineCode())).thenReturn(Optional.of(line));
            when(factoryRepository.findByFactoryCode(factory.getFactoryCode())).thenReturn(Optional.of(factory));
            when(itemRepository.findByItemCode(item.getItemCode())).thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemId(line.getId(),item.getId())).thenReturn(Optional.of(itemsLines));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(any(), anyList())).thenReturn(List.of());
            when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(List.of(equipment));

            // when
            GetProductionPlanResponseDto response =
                productionPlanService.updateProductionPlan(request, 1L, requestUser);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(PlanStatus.PENDING);
            assertThat(response.getPlannedQty()).isEqualTo(request.getPlannedQty());
            assertThat(response.getEndTime()).isAfter(request.getStartTime());

            verify(productionPlanRepository, times(1)).findById(1L);
        }


        @Test
        @DisplayName("생산계획 수정 실패 - 생산계획 ID 조회 실패 시 예외")
        void update_fail_plan_not_found() {
            // given
            UpdateProductionPlanRequestDto requestDto = createUpdateRequestDto();

            when(productionPlanRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                productionPlanService.updateProductionPlan(requestDto, 1L, requestUser)
            ).isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("실패 - 권한 오류 : 관리자가 PENDING/CONFIRMED 외 상태로 변경 시 실패")
        void update_fail_manager_status_forbidden() {
            // given
            UpdateProductionPlanRequestDto request = createUpdateRequestDto().toBuilder()
                .status(PlanStatus.RUNNING) // 허용되지 않음
                .build();

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(productionPlan));

            assertThatThrownBy(() ->
                productionPlanService.updateProductionPlan(request, 1L, requestUser))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN.getMessage());
        }

        @Test
        @DisplayName("수정 실패 - 계획 상태가 PENDING/CONFIRMED이 아니면 수정 불가")
        void update_fail_plan_not_updatable() {
            ProductionPlans plan = productionPlan.toBuilder()
                .status(PlanStatus.RUNNING) // isUpdatable() = false
                .build();

            when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
            UpdateProductionPlanRequestDto requestDto = createUpdateRequestDto();

            assertThatThrownBy(() ->
                productionPlanService.updateProductionPlan(requestDto, 1L, requestUser))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_BAD_REQUEST.getMessage());
        }

        @Test
        @DisplayName("생산계획 수정 실패 - 아이템-라인 조합이 없으면 실패")
        void update_fail_itemLine_not_found() {
            // given
            UpdateProductionPlanRequestDto requestDto = createUpdateRequestDto();

            when(productionPlanRepository.findById(productionPlan.getId())).thenReturn(Optional.of(productionPlan));
            when(lineRepository.findBylineCode(anyString())).thenReturn(Optional.of(line));
            when(factoryRepository.findByFactoryCode(anyString())).thenReturn(Optional.of(factory));
            when(itemRepository.findByItemCode(anyString())).thenReturn(Optional.of(item));
            when(userRepository.findByEmpNo(salesManager.getEmpNo())).thenReturn(Optional.of(salesManager));
            when(userRepository.findByEmpNo(productionManager.getEmpNo())).thenReturn(Optional.of(productionManager));

            when(itemLineRepository.findByLineIdAndItemId(1L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                productionPlanService.updateProductionPlan(requestDto, 1L, requestUser)
            )
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ItemLineErrorCode.ITEM_LINE_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("수정 실패 - 공장-라인 매칭 오류 시 실패")
        void update_fail_line_not_in_factory() {
            UpdateProductionPlanRequestDto requestDto = createUpdateRequestDto();

            Lines wrongLine = line.toBuilder()
                .factoryId(999L) // 다른 공장
                .factory(factory.toBuilder().id(999L).build())
                .build();

            ItemsLines otherItemLines = itemsLines.toBuilder()
                .lineId(wrongLine.getId())
                .line(wrongLine)
                .build();

            when(productionPlanRepository.findById(anyLong())).thenReturn(Optional.of(productionPlan));
            when(lineRepository.findBylineCode(anyString())).thenReturn(Optional.of(wrongLine));
            when(factoryRepository.findByFactoryCode(anyString())).thenReturn(Optional.of(factory));
            when(itemRepository.findByItemCode(anyString())).thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemId(any(), any()))
                .thenReturn(Optional.of(otherItemLines));
            when(userRepository.findByEmpNo(salesManager.getEmpNo())).thenReturn(Optional.of(salesManager));
            when(userRepository.findByEmpNo(productionManager.getEmpNo())).thenReturn(Optional.of(productionManager));

            assertThatThrownBy(() ->
                productionPlanService.updateProductionPlan(requestDto, 1L, requestUser)
            )
                .isInstanceOf(AppException.class)
                .hasMessageContaining(LineErrorCode.LINE_NOT_FOUND.getMessage());
        }

    }


    @Nested
    @DisplayName("생산계획 수정 – 생산계획 이동에 의한 수정 테스트")
    class UpdateProductionPlanShiftingTest {

        @BeforeEach
        void mockCommonDependencies() {
            // 모든 공통 레포 mock
            lenient().when(userRepository.findByEmpNo(any())).thenReturn(Optional.of(salesManager));
            lenient().when(lineRepository.findBylineCode(any())).thenReturn(Optional.of(line));
            lenient().when(itemRepository.findByItemCode(any())).thenReturn(Optional.of(item));
            lenient().when(factoryRepository.findByFactoryCode(any())).thenReturn(Optional.of(factory));
            lenient().when(itemLineRepository.findByLineIdAndItemId(any(), any())).thenReturn(Optional.of(itemsLines));
        }

        private ProductionPlans plan(Long id, String start, String end, PlanStatus status) {
            return productionPlan.toBuilder()
                .id(id)
                .startTime(time(start))
                .endTime(time(end))
                .status(status)
                .build();
        }

        private LocalDateTime time(String localTime) {
            return LocalDateTime.of(testDate, LocalTime.parse(localTime));
        }

        private UpdateProductionPlanRequestDto updateDto(String start) {
            return UpdateProductionPlanRequestDto.builder()
                .startTime(time(start))
                .status(PlanStatus.PENDING)
                .factoryCode("F1")
                .lineCode("L1")
                .itemCode("ITEM1")
                .build();
        }

        /*
        Case: A(수정 대상)
              A: 09:00~10:00 → 새로 10:00~11:00 로 변경
              B: 10:15~11:00 → A와 겹침 → delta 30분 적용 → 11:30~12:15 로 변경
         */
        @Test
        @DisplayName("겹치는 계획이 있으면 뒤로 밀린다")
        void shift_should_push_overlapped_plans() {
            // given
            ProductionPlans ppA = plan(1L, "09:00", "10:00", PlanStatus.PENDING);
            ProductionPlans ppB = plan(2L, "10:15", "11:00", PlanStatus.PENDING);

            UpdateProductionPlanRequestDto dto = updateDto("10:00");

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED))
            ).thenReturn(List.of(ppA, ppB));

            // when
            productionPlanService.updateProductionPlan(dto, 1L, salesManager);

            // then
            assertThat(ppB.getStartTime()).isEqualTo(time("11:30"));
            assertThat(ppB.getEndTime()).isEqualTo(time("12:15"));
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

            UpdateProductionPlanRequestDto dto = updateDto("09:00");

            when(productionPlanRepository.findById(1L))
                .thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED)
            )).thenReturn(List.of(ppA, ppB));

            productionPlanService.updateProductionPlan(dto, 1L, salesManager);

            assertThat(ppB.getStartTime()).isEqualTo(time("10:30"));
            assertThat(ppB.getEndTime()).isEqualTo(time("11:10"));
        }

        @Test
        @DisplayName("충돌도 없고 delta 조건이 넉넉하더라도 30분을 유지한다.")
        void shift_no_move_when_no_overlap_and_gap_ok() {
            ProductionPlans ppA = plan(1L, "09:00", "10:00", PlanStatus.PENDING);
            ProductionPlans ppB = plan(2L, "10:40", "11:00", PlanStatus.PENDING);

            UpdateProductionPlanRequestDto dto = updateDto("09:00");

            when(productionPlanRepository.findById(1L))
                .thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED)
            )).thenReturn(List.of(ppA, ppB));

            productionPlanService.updateProductionPlan(dto, 1L, salesManager);

            assertThat(ppB.getStartTime()).isEqualTo(time("10:30"));
            assertThat(ppB.getEndTime()).isEqualTo(time("10:50"));
        }

        @Test
        @DisplayName("PENDING 계획은 겹침 여부와 관계없이 A의 newEnd + 30분으로 이동된다")
        void pendingPlan_should_shift_from_newPlan_end() {
            // given
            ProductionPlans ppA = plan(1L, "09:00", "10:00", PlanStatus.PENDING);
            ProductionPlans ppB = plan(2L, "12:00", "13:00", PlanStatus.PENDING);

            UpdateProductionPlanRequestDto dto = updateDto("09:00");

            when(productionPlanRepository.findById(1L))
                .thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED)
            )).thenReturn(List.of(ppA, ppB));

            productionPlanService.updateProductionPlan(dto, 1L, salesManager);

            assertThat(ppB.getStartTime()).isEqualTo(time("10:30"));
            assertThat(ppB.getEndTime()).isEqualTo(time("11:30"));
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

            UpdateProductionPlanRequestDto dto = updateDto("09:00");

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED))
            ).thenReturn(List.of(ppA, ppB));

            assertThatThrownBy(() ->
                productionPlanService.updateProductionPlan(dto, 1L, salesManager)
            ).isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN.getMessage());
        }

        @Test
        @DisplayName("관리자라면 재배치 도중 CONFIRMED를 침해해도 수정이 가능하다.")
        void confirmedPlan_overlap_requestedByManager_should_shift() {
            requestUser = requestUser.toBuilder()
                .role(UserRole.ADMIN)
                .build();

            ProductionPlans ppA = plan(1L, "09:00", "10:00", PlanStatus.PENDING);
            ProductionPlans ppB = plan(2L, "10:00", "11:00", PlanStatus.CONFIRMED);

            UpdateProductionPlanRequestDto dto = updateDto("10:00");

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED))
            ).thenReturn(List.of(ppA, ppB));

            productionPlanService.updateProductionPlan(dto, 1L, requestUser);

            assertThat(ppB.getStartTime()).isEqualTo(time("11:30"));
            assertThat(ppB.getEndTime()).isEqualTo(time("12:30"));
        }

        @Test
        @DisplayName("자기 자신(newPlan)은 afterPlans 리스트에서 제거된다")
        void shift_should_remove_itself_from_afterPlans() {
            ProductionPlans ppA = plan(1L, "09:00", "10:00", PlanStatus.PENDING);
            ProductionPlans ppB = plan(2L, "10:30", "11:00", PlanStatus.PENDING);

            UpdateProductionPlanRequestDto dto = updateDto("09:00");

            when(productionPlanRepository.findById(1L))
                .thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED)
            )).thenReturn(List.of(ppA, ppB));

            productionPlanService.updateProductionPlan(dto, 1L, salesManager);

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
        @DisplayName("겹치지 않는 CONFIRMED 계획은 이동되지 않는다")
        void confirmedPlan_notOverlap_should_not_shift() {
            ProductionPlans ppA = plan(1L, "09:00", "10:00", PlanStatus.PENDING);
            ProductionPlans ppB = plan(2L, "12:00", "13:00", PlanStatus.CONFIRMED);

            UpdateProductionPlanRequestDto dto = updateDto("10:00");

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED))
            ).thenReturn(List.of(ppA, ppB));

            productionPlanService.updateProductionPlan(dto, 1L, requestUser);

            assertThat(ppB.getStartTime()).isEqualTo(time("12:00"));
            assertThat(ppB.getEndTime()).isEqualTo(time("13:00"));
        }

        @Test
        @DisplayName("다건 이동 - 관리자의 경우 PENDING 이후 CONFIRMED 조합에서 모두 정상 이동된다")
        void pendingThenConfirmed_should_shift_in_order() {
            requestUser = requestUser.toBuilder()
                .role(UserRole.ADMIN)
                .build();

            ProductionPlans ppA = plan(1L, "09:00", "10:00", PlanStatus.PENDING);
            ProductionPlans ppB = plan(2L, "10:10", "10:40", PlanStatus.PENDING);
            ProductionPlans ppC = plan(3L, "10:30", "11:00", PlanStatus.CONFIRMED);

            UpdateProductionPlanRequestDto dto = updateDto("10:00");

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(ppA));
            when(productionPlanRepository.findAllByStartTimeAndStatusAfterOrderByStartTimeAsc(
                dto.getStartTime(),
                List.of(PlanStatus.PENDING, PlanStatus.CONFIRMED))
            ).thenReturn(List.of(ppA, ppB, ppC));

            productionPlanService.updateProductionPlan(dto, 1L, requestUser);

            // B 이동
            assertThat(ppB.getStartTime()).isEqualTo(time("11:30"));
            assertThat(ppB.getEndTime()).isEqualTo(time("12:00"));

            // C 이동 (B 끝 기준)
            assertThat(ppC.getStartTime()).isEqualTo(time("12:30"));
            assertThat(ppC.getEndTime()).isEqualTo(time("13:00"));
        }
    }
}