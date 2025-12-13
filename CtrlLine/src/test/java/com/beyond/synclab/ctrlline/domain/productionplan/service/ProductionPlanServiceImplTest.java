package com.beyond.synclab.ctrlline.domain.productionplan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
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
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.AffectedPlanDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.DeleteProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanResponseDto;
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
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.UpdateProductionPlanStatusRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.errorcode.ProductionPlanErrorCode;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import com.beyond.synclab.ctrlline.domain.user.errorcode.UserErrorCode;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import com.beyond.synclab.ctrlline.domain.validator.DomainActivationValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock
    private ProductionPerformanceRepository productionPerformanceRepository;

    @Mock
    private ProductionPlanStatusNotificationService planStatusNotificationService;

    @Mock private ProductionPlanReconciliationService productionPlanReconciliationService;
    @Mock private DomainActivationValidator domainActivationValidator;

    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ObjectMapper objectMapper;

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
            objectMapper,
            productionPlanRepository,
            userRepository,
            lineRepository,
            factoryRepository,
            itemLineRepository,
            itemRepository,
            equipmentRepository,
            productionPerformanceRepository,
            planStatusNotificationService,
            testClock,
            productionPlanReconciliationService,
            redisTemplate,
            domainActivationValidator
        );

        lenient().when(productionPerformanceRepository.findRecentByLineId(anyLong(), any(Pageable.class)))
            .thenReturn(Collections.emptyList());

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
            .lineId(line.getId())
            .line(line)
            .equipmentPpm(BigDecimal.valueOf(1000))
            .totalCount(BigDecimal.valueOf(1000))
            .defectiveCount(BigDecimal.ZERO)
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
            .dueDate(testDate.plusDays(12))
            .startTime(testDateTime)
            .endTime(testDateTime.plusDays(1))
            .createdAt(testDateTime)
            .remark("testRemark")
            .build();
    }

    @Nested
    @DisplayName("생산계획생성 테스트")
    class CreateProductionPlanTest {

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
        @DisplayName("일반 생산계획 생성 성공")
        void createProductionPlan_success() {

            CreateProductionPlanRequestDto req = createRequestDto();

            when(userRepository.findByEmpNo(salesManager.getEmpNo()))
                .thenReturn(Optional.of(salesManager));
            when(userRepository.findByEmpNo(productionManager.getEmpNo()))
                .thenReturn(Optional.of(productionManager));
            when(lineRepository.findBylineCodeAndIsActiveTrue(line.getLineCode()))
                .thenReturn(Optional.of(line));
            when(itemRepository.findByItemCodeAndIsActiveTrue(item.getItemCode()))
                .thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemIdAndIsActiveTrue(line.getId(), item.getId()))
                .thenReturn(Optional.of(itemsLines));

            when(equipmentRepository.findAllByLineId(line.getId()))
                .thenReturn(List.of(equipment));

            // 최근계획 없음
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(
                anyLong(), anyList()
            )).thenReturn(Collections.emptyList());

            // save 시 ID 할당
            when(productionPlanRepository.save(any(ProductionPlans.class)))
                .thenAnswer(invocation -> {
                    ProductionPlans p = invocation.getArgument(0);
                    ReflectionTestUtils.setField(p, "id", 99L);
                    return p;
                });

            PlanScheduleChangeResponseDto result =
                productionPlanService.createProductionPlan(req, requestUser);

            assertThat(result.getPlanId()).isEqualTo(99L);
            assertThat(result.getPlanDocumentNo()).contains("2099/01/01-");

            verify(productionPlanRepository, times(1)).save(any());
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
            when(lineRepository.findBylineCodeAndIsActiveTrue(dto.getLineCode())).thenReturn(Optional.empty());

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
            when(lineRepository.findBylineCodeAndIsActiveTrue(line.getLineCode())).thenReturn(Optional.of(line));
            when(itemRepository.findByItemCodeAndIsActiveTrue(item.getItemCode())).thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemIdAndIsActiveTrue(line.getId(), item.getId())).thenReturn(Optional.of(itemsLines));
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
            when(lineRepository.findBylineCodeAndIsActiveTrue(line.getLineCode())).thenReturn(Optional.of(line));
            when(itemRepository.findByItemCodeAndIsActiveTrue(item.getItemCode())).thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemIdAndIsActiveTrue(line.getId(), item.getId())).thenReturn(Optional.of(itemsLines));
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
            when(lineRepository.findBylineCodeAndIsActiveTrue(line.getLineCode())).thenReturn(Optional.of(line));
            when(itemRepository.findByItemCodeAndIsActiveTrue(item.getItemCode())).thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemIdAndIsActiveTrue(line.getId(), item.getId()))
                .thenReturn(Optional.of(itemsLines));
            when(equipmentRepository.findAllByLineId(line.getId())).thenReturn(List.of(equipment));
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(
                eq(line.getId()), anyList()
            )).thenReturn(List.of(existingPlan));
            when(productionPlanRepository.findByDocumentNoByPrefix(anyString())).thenReturn(List.of(existingPlan.getDocumentNo()));

            when(productionPlanRepository.save(any(ProductionPlans.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            PlanScheduleChangeResponseDto result = productionPlanService.createProductionPlan(requestDto, requestUser);

            // then
            assertThat(result).isNotNull();
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
    @DisplayName("긴급 생산계획 생성 테스트")
    class CreateEmergentPlanTest {

        @Test
        @DisplayName("긴급 생산계획 생성 성공 - 맨 앞 배치 + 전체 Shift")
        void createEmergentPlan_success() {

            Users admin = Users.builder()
                .id(10L)
                .role(UserRole.ADMIN)
                .empNo("900001")
                .build();

            CreateProductionPlanRequestDto req = CreateProductionPlanRequestDto.builder()
                .salesManagerNo(salesManager.getEmpNo())
                .productionManagerNo(productionManager.getEmpNo())
                .lineCode(line.getLineCode())
                .itemCode(item.getItemCode())
                .plannedQty(BigDecimal.valueOf(100))
                .isEmergent(true)
                .build();

            when(userRepository.findByEmpNo(salesManager.getEmpNo()))
                .thenReturn(Optional.of(salesManager));
            when(userRepository.findByEmpNo(productionManager.getEmpNo()))
                .thenReturn(Optional.of(productionManager));
            when(lineRepository.findBylineCodeAndIsActiveTrue(line.getLineCode()))
                .thenReturn(Optional.of(line));
            when(itemRepository.findByItemCodeAndIsActiveTrue(item.getItemCode()))
                .thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemIdAndIsActiveTrue(line.getId(), item.getId()))
                .thenReturn(Optional.of(itemsLines));

            when(equipmentRepository.findAllByLineId(line.getId()))
                .thenReturn(List.of(equipment));

            // 기존 계획이 하나 있다고 가정 → shift 발생 여부 검증 가능
            ProductionPlans existingPlan = ProductionPlans.builder()
                .id(50L)
                .itemLine(itemsLines)
                .startTime(testDateTime.plusHours(2))  // 02:00
                .endTime(testDateTime.plusHours(4))    // 04:00
                .status(PlanStatus.PENDING)
                .plannedQty(BigDecimal.valueOf(200))
                .build();

            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(
                eq(line.getId()), anyList()))
                .thenReturn(List.of(existingPlan));

            // saveAll 시 확인용으로 그대로 반환한다.
            when(productionPlanRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            PlanScheduleChangeResponseDto result =
                productionPlanService.createProductionPlan(req, admin);

            assertThat(result.getAffectedPlans()).isNotEmpty();
            assertThat(result.getPlanDocumentNo()).contains("2099/01/01");

            verify(productionPlanRepository, times(1)).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("생산계획 업데이트 테스트")
    class UpdateProductionPlanTest {

        private UpdateProductionPlanRequestDto createUpdateDto(
            LocalDateTime start, BigDecimal qty
        ) {
            return UpdateProductionPlanRequestDto.builder()
                .startTime(start)         // null 가능 → 유지
                .plannedQty(qty)          // null 가능 → 유지
                .lineCode(line.getLineCode())
                .itemCode(item.getItemCode())
                .dueDate(testDate)
                .status(null)
                .build();
        }

        private void mockCommonFind() {
            lenient().when(lineRepository.findBylineCodeAndIsActiveTrue(line.getLineCode()))
                .thenReturn(Optional.of(line));
            lenient().when(itemRepository.findByItemCodeAndIsActiveTrue(item.getItemCode()))
                .thenReturn(Optional.of(item));
            lenient().when(itemLineRepository.findByLineIdAndItemIdAndIsActiveTrue(line.getId(), item.getId()))
                .thenReturn(Optional.of(itemsLines));
            lenient().when(equipmentRepository.findAllByLineId(line.getId()))
                .thenReturn(List.of(equipment));
        }

        /* ===========================================================
         * 1. 정상 업데이트 - 시작시간 변경
         * =========================================================== */
        @Test
        @DisplayName("정상 업데이트 - 시작시간 변경 시 뒤 계획 Shift")
        void update_success_shiftOccurs() {

            mockCommonFind();

            ProductionPlans nextPlan = ProductionPlans.builder()
                .id(2L)
                .startTime(testDateTime.plusHours(3))
                .endTime(testDateTime.plusHours(5))
                .status(PlanStatus.PENDING)
                .itemLine(itemsLines)
                .plannedQty(BigDecimal.valueOf(100))
                .build();

            when(productionPlanRepository.findById(1L))
                .thenReturn(Optional.of(productionPlan));

            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(
                eq(line.getId()), anyList()))
                .thenReturn(List.of(productionPlan, nextPlan));

            when(productionPlanRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProductionPlanRequestDto req =
                createUpdateDto(testDateTime.plusHours(1), null);

            PlanScheduleChangeResponseDto result =
                productionPlanService.updateProductionPlan(req, 1L, requestUser);

            assertThat(result.getAffectedPlans()).isNotEmpty();
            verify(planStatusNotificationService, times(1)).notifyScheduleChange(any(), any(), any());
        }

        /* ===========================================================
         * 2. 정상 업데이트 - 수량 변경 → endTime 재계산
         * =========================================================== */
        @Test
        @DisplayName("정상 업데이트 - 수량 변경 시 종료시간 재계산")
        void update_success_qtyChangeRecalculatesEnd() {

            mockCommonFind();

            ProductionPlans original = productionPlan;

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(original));
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(eq(line.getId()), anyList()))
                .thenReturn(List.of(original));

            when(productionPlanRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProductionPlanRequestDto req =
                createUpdateDto(null, BigDecimal.valueOf(999));

            PlanScheduleChangeResponseDto result =
                productionPlanService.updateProductionPlan(req, 1L, requestUser);

            assertThat(result.getPlanId()).isEqualTo(original.getId());
            verify(planStatusNotificationService, times(1)).notifyScheduleChange(any(), any(), any());
        }

        /* ===========================================================
         * 3. 과거 시간으로 업데이트 시 예외
         * =========================================================== */
        @Test
        @DisplayName("업데이트 실패 - 시작시간이 현재 시간보다 이전")
        void update_fail_startTimeInPast() {

            UpdateProductionPlanRequestDto req =
                createUpdateDto(testDateTime.minusHours(1), null);

            assertThatThrownBy(() ->
                productionPlanService.updateProductionPlan(req, 1L, requestUser)
            ).isInstanceOf(AppException.class)
                .hasMessageContaining(CommonErrorCode.INVALID_REQUEST.getMessage());
        }

        /* ===========================================================
         * 4. 존재하지 않는 계획 ID
         * =========================================================== */
        @Test
        @DisplayName("업데이트 실패 - PlanId 없음")
        void update_fail_planNotFound() {

            when(productionPlanRepository.findById(999L)).thenReturn(Optional.empty());

            UpdateProductionPlanRequestDto req = createUpdateDto(null, null);

            assertThatThrownBy(() ->
                productionPlanService.updateProductionPlan(req, 999L, requestUser)
            ).isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND.getMessage());
        }

        /* ===========================================================
         * 5. Updatable 아닌 상태면 수정 불가
         * =========================================================== */
        @Test
        @DisplayName("업데이트 실패 - Updatable false")
        void update_fail_notUpdatable() {

            ProductionPlans locked = ProductionPlans.builder()
                .id(10L)
                .itemLine(itemsLines)
                .status(PlanStatus.COMPLETED)   // 완료 상태 → update 불가
                .startTime(testDateTime)
                .endTime(testDateTime.plusHours(2))
                .build();

            when(productionPlanRepository.findById(10L)).thenReturn(Optional.of(locked));

            UpdateProductionPlanRequestDto req = createUpdateDto(null, null);

            assertThatThrownBy(() ->
                productionPlanService.updateProductionPlan(req, 10L, requestUser)
            ).isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_UPDATABLE.getMessage());
        }

        /* ===========================================================
         * 6. MANAGER 가 CONFIRMED 계획을 밀려고 하면 예외
         * =========================================================== */
        @Test
        @DisplayName("업데이트 실패 - Manager는 CONFIRMED 계획 밀기 금지")
        void update_fail_managerCannotPushConfirmed() {

            mockCommonFind();

            // 뒤의 계획이 확정(CONFIRMED)
            ProductionPlans confirmedNext = ProductionPlans.builder()
                .id(2L)
                .startTime(testDateTime.plusHours(2))
                .endTime(testDateTime.plusHours(4))
                .status(PlanStatus.CONFIRMED)
                .itemLine(itemsLines)
                .plannedQty(BigDecimal.valueOf(100))
                .build();

            when(productionPlanRepository.findById(1L))
                .thenReturn(Optional.of(productionPlan));

            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(
                eq(line.getId()), anyList()))
                .thenReturn(List.of(productionPlan, confirmedNext));

            UpdateProductionPlanRequestDto req =
                createUpdateDto(testDateTime.plusHours(1), null);

            Users manager = Users.builder().role(UserRole.MANAGER).build();

            assertThatThrownBy(() ->
                productionPlanService.updateProductionPlan(req, 1L, manager)
            ).isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN.getMessage());
        }

        /* ===========================================================
         * 7. itemLine 변경 시 itemLine 존재하지 않으면 오류
         * =========================================================== */
        @Test
        @DisplayName("업데이트 실패 - itemLine 없음")
        void update_fail_itemLineNotFound() {

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(productionPlan));
            when(lineRepository.findBylineCodeAndIsActiveTrue(anyString())).thenReturn(Optional.of(line));
            when(itemRepository.findByItemCodeAndIsActiveTrue(anyString())).thenReturn(Optional.of(item));

            // itemLine 없음
            when(itemLineRepository.findByLineIdAndItemIdAndIsActiveTrue(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

            UpdateProductionPlanRequestDto req =
                createUpdateDto(null, null);

            assertThatThrownBy(() ->
                productionPlanService.updateProductionPlan(req, 1L, requestUser)
            ).isInstanceOf(AppException.class)
                .hasMessageContaining(ItemLineErrorCode.ITEM_LINE_NOT_FOUND.getMessage());
        }

        /* ===========================================================
         * 8. dueDate 초과 시 알림 호출
         * =========================================================== */
        @Test
        @DisplayName("정상 업데이트 - 종료시간이 납기일 초과하면 DueDate 알림 호출")
        void update_success_dueDateExceededTriggersNotification() {

            mockCommonFind();

            ProductionPlans plan = productionPlan;

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(eq(line.getId()), anyList()))
                .thenReturn(List.of(plan));

            when(productionPlanRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProductionPlanRequestDto req =
                createUpdateDto(testDateTime.plusDays(120), null);

            productionPlanService.updateProductionPlan(req, 1L, requestUser);

            verify(planStatusNotificationService, atLeastOnce()).notifyScheduleChange(any(), any(), any());
            verify(planStatusNotificationService, atLeastOnce()).notifyDueDateExceeded(any());
        }

        /* ===========================================================
         * 9. compact로 인해 idle 제거되는지 확인
         * =========================================================== */
        @Test
        @DisplayName("정상 업데이트 - compact로 idle 제거")
        void update_success_compactRemovesIdle() {

            mockCommonFind();

            ProductionPlans plan1 = productionPlan;

            ProductionPlans plan2 = ProductionPlans.builder()
                .id(2L)
                .itemLine(itemsLines)
                .startTime(testDateTime.plusHours(3))
                .endTime(testDateTime.plusHours(5))
                .status(PlanStatus.PENDING)
                .build();

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(plan1));
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(any(), anyList()))
                .thenReturn(List.of(plan1, plan2));

            when(productionPlanRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProductionPlanRequestDto req =
                createUpdateDto(testDateTime.plusHours(1), null);

            PlanScheduleChangeResponseDto result =
                productionPlanService.updateProductionPlan(req, 1L, requestUser);

            assertThat(result.getAffectedPlans()).isNotEmpty();
        }

        /* ===========================================================
         * 10. 시작시간 null → 기존 값 유지
         * =========================================================== */
        @Test
        @DisplayName("정상 업데이트 - startTime=null 이면 기존 유지")
        void update_success_startTimeNotChanged() {

            mockCommonFind();

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(productionPlan));
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(any(), anyList()))
                .thenReturn(List.of(productionPlan));

            when(productionPlanRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProductionPlanRequestDto req =
                createUpdateDto(null, null); // startTime 그대로 유지

            productionPlanService.updateProductionPlan(req, 1L, requestUser);

            assertThat(productionPlan.getStartTime()).isEqualTo(testDateTime);
        }

        /* ===========================================================
         * 11. 수량 null → 기존 값 유지
         * =========================================================== */
        @Test
        @DisplayName("정상 업데이트 - plannedQty=null 이면 기존 유지")
        void update_success_qtyNotChanged() {

            mockCommonFind();

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(productionPlan));
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(any(), anyList()))
                .thenReturn(List.of(productionPlan));

            when(productionPlanRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProductionPlanRequestDto req = createUpdateDto(null, null);

            productionPlanService.updateProductionPlan(req, 1L, requestUser);

            assertThat(productionPlan.getPlannedQty()).isEqualTo(BigDecimal.valueOf(100));
        }

        /* ===========================================================
         * 12. role=ADMIN 은 CONFIRMED 계획도 밀 수 있음
         * =========================================================== */
        @Test
        @DisplayName("정상 업데이트 - ADMIN 은 CONFIRMED 계획도 밀기 가능")
        void update_success_adminCanPushConfirmed() {

            mockCommonFind();

            Users admin = Users.builder().role(UserRole.ADMIN).build();

            ProductionPlans confirmed = ProductionPlans.builder()
                .id(2L)
                .status(PlanStatus.CONFIRMED)
                .startTime(testDateTime.plusHours(2))
                .endTime(testDateTime.plusHours(4))
                .itemLine(itemsLines)
                .plannedQty(BigDecimal.valueOf(100))
                .build();

            when(productionPlanRepository.findById(1L))
                .thenReturn(Optional.of(productionPlan));
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(any(), anyList()))
                .thenReturn(List.of(productionPlan, confirmed));

            when(productionPlanRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProductionPlanRequestDto req =
                createUpdateDto(testDateTime.plusHours(1), null);

            PlanScheduleChangeResponseDto result =
                productionPlanService.updateProductionPlan(req, 1L, admin);

            assertThat(result.getAffectedPlans()).isNotEmpty();
        }

        /* ===========================================================
         * 13. lineCode 변경 → itemLine 재조회 정상
         * =========================================================== */
        @Test
        @DisplayName("정상 업데이트 - lineCode 변경 시 새 itemLine 매핑")
        void update_success_lineChanged() {

            Lines newLine = Lines.builder().id(99L).lineCode("NEWLINE").build();

            ItemsLines newItemLine = ItemsLines.builder()
                .id(999L)
                .line(newLine)
                .lineId(newLine.getId())
                .item(item)
                .itemId(item.getId())
                .build();

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(productionPlan));

            when(lineRepository.findBylineCodeAndIsActiveTrue("NEWLINE")).thenReturn(Optional.of(newLine));
            when(itemRepository.findByItemCodeAndIsActiveTrue(item.getItemCode())).thenReturn(Optional.of(item));
            when(itemLineRepository.findByLineIdAndItemIdAndIsActiveTrue(newLine.getId(), item.getId()))
                .thenReturn(Optional.of(newItemLine));


            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(any(), anyList()))
                .thenReturn(List.of(productionPlan));

            when(productionPlanRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProductionPlanRequestDto req =
                UpdateProductionPlanRequestDto.builder()
                    .lineCode("NEWLINE")
                    .itemCode(item.getItemCode())
                    .build();

            productionPlanService.updateProductionPlan(req, 1L, requestUser);

            assertThat(productionPlan.getItemLine().getLine()).isEqualTo(newLine);
        }

        /* ===========================================================
         * 14. itemCode 변경 → itemLine 재조회
         * =========================================================== */
        @Test
        @DisplayName("정상 업데이트 - itemCode 변경 시 새 itemLine 매핑")
        void update_success_itemChanged() {

            Items newItem = Items.builder().id(55L).itemCode("NEWITEM").build();
            ItemsLines newItemLine = ItemsLines.builder()
                .id(555L)
                .line(line)
                .lineId(line.getId())
                .item(newItem)
                .itemId(newItem.getId())
                .build();

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(productionPlan));

            when(lineRepository.findBylineCodeAndIsActiveTrue(line.getLineCode())).thenReturn(Optional.of(line));
            when(itemRepository.findByItemCodeAndIsActiveTrue("NEWITEM")).thenReturn(Optional.of(newItem));
            when(itemLineRepository.findByLineIdAndItemIdAndIsActiveTrue(line.getId(), newItem.getId()))
                .thenReturn(Optional.of(newItemLine));

            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(any(), anyList()))
                .thenReturn(List.of(productionPlan));

            when(productionPlanRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProductionPlanRequestDto req =
                UpdateProductionPlanRequestDto.builder()
                    .lineCode(line.getLineCode())
                    .itemCode("NEWITEM")
                    .build();

            productionPlanService.updateProductionPlan(req, 1L, requestUser);

            assertThat(productionPlan.getItemLine().getItem()).isEqualTo(newItem);
        }

        /* ===========================================================
         * 15. Shift 적용 후 compact 부작용 없는지(순서 유지)
         * =========================================================== */
        @Test
        @DisplayName("정상 업데이트 - shift + compact 후 계획 순서 보존")
        void update_success_planOrderMaintained() {

            mockCommonFind();

            ProductionPlans plan1 = productionPlan;

            ProductionPlans plan2 = ProductionPlans.builder()
                .id(2L)
                .itemLine(itemsLines)
                .status(PlanStatus.PENDING)
                .startTime(testDateTime.plusHours(5))
                .endTime(testDateTime.plusHours(7))
                .build();

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(plan1));
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(any(), anyList()))
                .thenReturn(List.of(plan1, plan2));

            when(productionPlanRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProductionPlanRequestDto req =
                createUpdateDto(testDateTime.plusHours(2), null);

            PlanScheduleChangeResponseDto result =
                productionPlanService.updateProductionPlan(req, 1L, requestUser);

            assertThat(result.getAffectedPlans()).hasSizeGreaterThan(0);
        }

        private LocalDateTime time(String hhmm) {
            return LocalDateTime.of(
                2099, 1, 1,
                Integer.parseInt(hhmm.substring(0, 2)),
                Integer.parseInt(hhmm.substring(3, 5))
            );
        }

        private ProductionPlans plan(Long id, String start, String end) {
            return ProductionPlans.builder()
                .id(id)
                .itemLine(itemsLines)
                .status(PlanStatus.PENDING)
                .plannedQty(BigDecimal.valueOf(100))
                .startTime(time(start))
                .endTime(time(end))
                .build();
        }

        private AffectedPlanDto findAffected(PlanScheduleChangeResponseDto result, Long id) {
            return result.getAffectedPlans().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Affected plan not found: " + id));
        }

        @Test
        @DisplayName("compact 고려 — 뒤로 미뤄도 compact로 인해 다시 앞에 붙는다")
        void scenario_pendingMoveLater_compactPullsForward() {

            mockCommonFind();

            // p1: 09:00~11:00
            ProductionPlans p1 = plan(1L, "09:00", "11:00");

            // p2: 11:00~13:00 (업데이트 대상)
            ProductionPlans p2 = plan(2L, "11:00", "13:00");

            // p3: 13:00~15:00
            ProductionPlans p3 = plan(3L, "13:00", "15:00");

            when(productionPlanRepository.findById(2L)).thenReturn(Optional.of(p2));
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(eq(line.getId()), anyList()))
                .thenReturn(List.of(p1, p2, p3));
            when(productionPlanRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            // 업데이트 요청: p2를 뒤로 미루기(12:00 시작시키기 요청)
            UpdateProductionPlanRequestDto req = UpdateProductionPlanRequestDto.builder()
                .startTime(testDateTime.plusHours(3)) // 12:00
                .lineCode(line.getLineCode())
                .itemCode(item.getItemCode())
                .build();

            PlanScheduleChangeResponseDto result =
                productionPlanService.updateProductionPlan(req, 2L, requestUser);

            assertThat(result.getAffectedPlans()).isEmpty();  // compact로 인해 변경점 없음
        }

        @Test
        @DisplayName("compact — 첫 계획이 09:30 이후면 강제로 09:30으로 당겨진다")
        void scenario_firstPlanPulledToCompactStart() {

            mockCommonFind();

            ProductionPlans p1 = plan(1L, "10:00", "12:00");   // 현재시간+30분 = 09:30 → compact 적용
            ProductionPlans p2 = plan(2L, "12:00", "14:00");
            ProductionPlans p3 = plan(3L, "14:00", "16:00");

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(p1));
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(eq(line.getId()), anyList()))
                .thenReturn(List.of(p1, p2, p3));
            when(productionPlanRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            UpdateProductionPlanRequestDto req = UpdateProductionPlanRequestDto.builder()
                .startTime(time("10:00"))
                .lineCode(line.getLineCode())
                .itemCode(item.getItemCode())
                .build();

            PlanScheduleChangeResponseDto result =
                productionPlanService.updateProductionPlan(req, 1L, requestUser);

            var a1 = findAffected(result, 1L);
            var a2 = findAffected(result, 2L);
            var a3 = findAffected(result, 3L);

            assertThat(a1.getNewStartTime()).isEqualTo(time("09:30"));
            assertThat(a2.getNewStartTime()).isEqualTo(time("11:30"));
            assertThat(a3.getNewStartTime()).isEqualTo(time("13:30"));
        }

        @Test
        @DisplayName("compact — 업데이트한 계획이 앞으로 당겨지면 뒤 계획들도 연쇄적으로 앞당겨진다")
        void scenario_moveEarlier_compactPullsChainForward() {

            mockCommonFind();

            ProductionPlans p1 = plan(1L, "11:00", "13:00");   // update 대상
            ProductionPlans p2 = plan(2L, "13:00", "15:00");
            ProductionPlans p3 = plan(3L, "15:00", "17:00");

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(p1));
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(eq(line.getId()), anyList()))
                .thenReturn(List.of(p1, p2, p3));
            when(productionPlanRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            // 앞으로 당기기 요청 → compact 로 모두 이동
            UpdateProductionPlanRequestDto req = UpdateProductionPlanRequestDto.builder()
                .startTime(time("09:00"))
                .lineCode(line.getLineCode())
                .itemCode(item.getItemCode())
                .build();

            PlanScheduleChangeResponseDto result =
                productionPlanService.updateProductionPlan(req, 1L, requestUser);

            var a1 = findAffected(result, 1L);
            var a2 = findAffected(result, 2L);
            var a3 = findAffected(result, 3L);

            // compact 기준 09:30부터 시작
            assertThat(a1.getNewStartTime()).isEqualTo(time("09:00"));
            assertThat(a2.getNewStartTime()).isEqualTo(time("11:00"));
            assertThat(a3.getNewStartTime()).isEqualTo(time("13:00"));
        }

        @Test
        @DisplayName("duration 증가 — 뒤 계획 모두 밀렸다가 compact 재정렬됨")
        void scenario_durationIncrease_shiftsAndCompact() {

            mockCommonFind();

            ProductionPlans p1 = plan(1L, "09:00", "11:00");   // update target
            ProductionPlans p2 = plan(2L, "11:00", "13:00");
            ProductionPlans p3 = plan(3L, "13:00", "15:00");

            when(productionPlanRepository.findById(1L)).thenReturn(Optional.of(p1));
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(eq(line.getId()), anyList()))
                .thenReturn(List.of(p1, p2, p3));
            when(productionPlanRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            // duration 증가 → plannedQty 증가
            UpdateProductionPlanRequestDto req = UpdateProductionPlanRequestDto.builder()
                .plannedQty(BigDecimal.valueOf(500000)) // huge → endTime 크게 증가
                .lineCode(line.getLineCode())
                .itemCode(item.getItemCode())
                .build();

            PlanScheduleChangeResponseDto result =
                productionPlanService.updateProductionPlan(req, 1L, requestUser);

            // compact 기준
            var a1 = findAffected(result, 1L);
            var a2 = findAffected(result, 2L);
            var a3 = findAffected(result, 3L);

            assertThat(a1.getNewStartTime()).isEqualTo(time("09:00")); // unchanged start
            assertThat(a2.getNewStartTime()).isEqualTo(a1.getNewEndTime()); // 붙음
            assertThat(a3.getNewStartTime()).isEqualTo(a2.getNewEndTime()); // 붙음
        }

        @Test
        @DisplayName("compact — 중간 계획을 크게 뒤로 밀어도 compact가 모든 gap 제거")
        void scenario_bigShiftThenCompact() {

            mockCommonFind();

            ProductionPlans p1 = plan(1L, "09:00", "11:00");
            ProductionPlans p2 = plan(2L, "11:00", "13:00"); // updated
            ProductionPlans p3 = plan(3L, "13:00", "15:00");

            when(productionPlanRepository.findById(2L)).thenReturn(Optional.of(p2));
            when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(eq(line.getId()), anyList()))
                .thenReturn(List.of(p1, p2, p3));
            when(productionPlanRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            // 11→16 으로 크게 밀기 요청
            UpdateProductionPlanRequestDto req = UpdateProductionPlanRequestDto.builder()
                .startTime(time("16:00"))
                .lineCode(line.getLineCode())
                .itemCode(item.getItemCode())
                .build();

            PlanScheduleChangeResponseDto result =
                productionPlanService.updateProductionPlan(req, 2L, requestUser);

            var a2 = findAffected(result, 2L);
            var a3 = findAffected(result, 3L);

            // compact 기준 정답
            assertThat(a2.getNewStartTime()).isEqualTo(time("13:00"));
            assertThat(a3.getNewStartTime()).isEqualTo(time("11:00"));
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
            ProductionPerformances productionPerformances = ProductionPerformances.builder()
                    .id(1L)
                    .productionPlanId(planId)
                    .endTime(testDateTime)
                    .build();

            when(productionPlanRepository.findById(planId))
                .thenReturn(Optional.of(productionPlans));
            when(productionPerformanceRepository.findByProductionPlanIdAndIsDeletedFalse(planId))
                .thenReturn(Optional.of(productionPerformances));
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
                .status(List.of(PlanStatus.PENDING))
                .factoryName(factory.getFactoryName())
                .salesManagerName(salesManager.getName())
                .productionManagerName(productionManager.getName())
                .itemName(item.getItemName())
                .dueDateFrom(testDate)
                .dueDateTo(testDate.plusDays(1))
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
                .status(List.of(PlanStatus.PENDING))
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
    @DisplayName("생산계획현황 조회")
    class GetAllProductionPlanTest {
        private GetAllProductionPlanRequestDto requestDto;

        @BeforeEach
        void setUp() {
            requestDto = GetAllProductionPlanRequestDto.builder()
                .factoryName("A공장")
                .lineName("1호라인")
                .itemName("샘플제품")
                .itemCode("ITEM001")
                .salesManagerName("김영업")
                .productionManagerName("박생산")
                .dueDate(testDate)
                .startTime(testDateTime)
                .endTime(testDateTime)
                .build();
        }

        @Test
        @DisplayName("정상 파라미터로 전체 목록 조회 성공")
        void getAllProductionPlan_success() {
            // given
            ProductionPlans planA = productionPlan.toBuilder()
                .documentNo("2099/01/01-1")
                .status(PlanStatus.PENDING)
                .build();

            ProductionPlans planB = productionPlan.toBuilder()
                .documentNo("2099/01/01-2")
                .status(PlanStatus.PENDING)
                .build();

            // Repository mock 결과: documentNo DESC (PP-002 → PP-001)
            when(productionPlanRepository.findAll(
                ArgumentMatchers.<Specification<ProductionPlans>>any(),
                ArgumentMatchers.any(Sort.class)))
                .thenReturn(List.of(planB, planA));

            // when
            List<GetAllProductionPlanResponseDto> result =
                productionPlanService.getAllProductionPlan(requestDto);

            // then
            assertThat(result).hasSize(2);

            // 매핑 검증
            assertThat(result.get(0).getDocumentNo()).isEqualTo("2099/01/01-2");
            assertThat(result.get(1).getDocumentNo()).isEqualTo("2099/01/01-1");

            // 정렬 조건(documentNo DESC) 검증
            verify(productionPlanRepository, times(1))
                .findAll(
                    ArgumentMatchers.<Specification<ProductionPlans>>any(),
                    ArgumentMatchers.<Sort>argThat(sort -> {
                        Sort.Order order = sort.getOrderFor("createdAt");
                        return order != null && order.getDirection() == Sort.Direction.DESC;
                    })
                );
        }


        @Test
        @DisplayName("파라미터 없이 전체 조회 시 기본 정렬(documentNo DESC) 적용")
        void getAllProductionPlan_defaultSort() {
            // given
            GetAllProductionPlanRequestDto dto = GetAllProductionPlanRequestDto.builder()
                .build();

            ProductionPlans planA = productionPlan.toBuilder().documentNo("2099/01/01-2").build();
            ProductionPlans planB = productionPlan.toBuilder().documentNo("2099/01/01-1").build();

            when(productionPlanRepository.findAll(
                ArgumentMatchers.<Specification<ProductionPlans>>any(),
                ArgumentMatchers.any(Sort.class)))
                .thenReturn(List.of(planA, planB));

            // when
            List<GetAllProductionPlanResponseDto> result =
                productionPlanService.getAllProductionPlan(dto);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getDocumentNo()).isEqualTo("2099/01/01-2");

            // 정렬 검증
            verify(productionPlanRepository).findAll(
                ArgumentMatchers.<Specification<ProductionPlans>>any(),
                ArgumentMatchers.<Sort>argThat(sort -> {
                    Sort.Order order = sort.getOrderFor("createdAt");
                    return order != null && order.getDirection() == Sort.Direction.DESC;
                })
            );
        }
    }

    @Nested
    @DisplayName("생산계획 일정 조회")
    class GetProductionPlanScheduleTest {
        @Test
        @DisplayName("생산 계획 일정 조회 성공 - 기본 조회")
        void getProductionPlanSchedule_success() {
            // given
            GetProductionPlanScheduleRequestDto requestDto = GetProductionPlanScheduleRequestDto.builder()
                .factoryName("A공장")
                .lineName("1호라인")
                .startTime(LocalDateTime.now(testClock))
                .endTime(LocalDateTime.now(testClock).plusHours(2))
                .build();

            List<ProductionPlans> mockResult = List.of(productionPlan);

            when(productionPlanRepository.findAll(
                ArgumentMatchers.<Specification<ProductionPlans>>any(),
                ArgumentMatchers.any(Sort.class)))
                .thenReturn(mockResult);

            // when
            List<GetProductionPlanScheduleResponseDto> result =
                productionPlanService.getProductionPlanSchedule(requestDto);

            assertThat(result).hasSize(1);
            GetProductionPlanScheduleResponseDto dto = result.getFirst();
            assertThat(dto.getDocumentNo()).isEqualTo(productionPlan.getDocumentNo());
            assertThat(dto.getFactoryName()).isEqualTo(productionPlan.getItemLine().getLine().getFactory().getFactoryName());
            assertThat(dto.getStartTime()).isEqualTo(productionPlan.getStartTime());
            assertThat(dto.getEndTime()).isEqualTo(productionPlan.getEndTime());

            // repository 호출 검증 (ASC 정렬)
            verify(productionPlanRepository, times(1))
                .findAll(
                    ArgumentMatchers.<Specification<ProductionPlans>>any(),
                    ArgumentMatchers.<Sort>argThat(sort -> {
                        Sort.Order order = sort.getOrderFor("startTime");
                        return order != null && order.getDirection() == Sort.Direction.ASC;
                    })
                );
        }

        @Test
        @DisplayName("생산 계획 일정 조회 실패 - 조회 기간 31일 초과")
        void getProductionPlanSchedule_fail_maxRangeExceeded() {
            // given
            GetProductionPlanScheduleRequestDto requestDto = GetProductionPlanScheduleRequestDto.builder()
                .factoryName("A공장")
                .lineName("1호라인")
                .startTime(testDateTime)
                .endTime(testDateTime.plusDays(32))
                .build();

            // when & then
            assertThatThrownBy(() -> productionPlanService.getProductionPlanSchedule(requestDto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_BAD_REQUEST.getMessage());
        }
    }

    @Nested
    @DisplayName("생산계획 종료시간 반환 테스트")
    class GetProductionPlanEndTimeTest {

        @Test
        @DisplayName("종료시간 반환 성공")
        void testGetProductionPlanEndTime_success() {
            // given
            GetProductionPlanEndTimeRequestDto request = GetProductionPlanEndTimeRequestDto.builder()
                .lineCode("LINE001")
                .plannedQty(BigDecimal.valueOf(300))
                .startTime(testDateTime)
                .build();

            Equipments eq1 = equipment.toBuilder()
                .id(1L)
                .equipmentPpm(BigDecimal.valueOf(50))
                .build();

            Equipments eq2 = equipment.toBuilder()
                .id(2L)
                .equipmentPpm(BigDecimal.valueOf(100))
                .build();

            when(lineRepository.findBylineCodeAndIsActiveTrue("LINE001")).thenReturn(Optional.of(line));
            when(equipmentRepository.findAllByLineId(1L)).thenReturn(List.of(eq1, eq2));

            // when
            GetProductionPlanEndTimeResponseDto response = productionPlanService.getProductionPlanEndTime(request);

            // then
            // 장비 총 PPM = 50 + 100 = 150
            // 트레이 용량 36 -> 300ea 생산을 위해 9개 트레이 가동(324ea)
            // 소요 시간 = 324 / 150 = 2.16분 + Stage 진행 시간 (약 1.08분) -> 3.24분 → 4분으로 올림
            assertThat(response.getEndTime()).isEqualTo(testDateTime.plusMinutes(34));
        }

        @Test
        @DisplayName("종료시간 반환 - 생산실적 기반 보정")
        void testGetProductionPlanEndTime_withPerformanceCalibration() {
            // given
            GetProductionPlanEndTimeRequestDto request = GetProductionPlanEndTimeRequestDto.builder()
                .lineCode("LINE001")
                .plannedQty(BigDecimal.valueOf(100))
                .startTime(testDateTime)
                .build();

            Equipments eq1 = equipment.toBuilder()
                .id(1L)
                .equipmentPpm(BigDecimal.valueOf(80))
                .build();
            Equipments eq2 = equipment.toBuilder()
                .id(2L)
                .equipmentPpm(BigDecimal.valueOf(90))
                .build();

            ProductionPerformances performance = ProductionPerformances.builder()
                .id(100L)
                .productionPlanId(200L)
                .performanceDocumentNo("PERF-001")
                .totalQty(BigDecimal.valueOf(220))
                .performanceQty(BigDecimal.valueOf(200))
                .performanceDefectiveRate(BigDecimal.valueOf(10))
                .startTime(testDateTime.minusMinutes(40))
                .endTime(testDateTime)
                .build();

            when(lineRepository.findBylineCodeAndIsActiveTrue("LINE001")).thenReturn(Optional.of(line));
            when(equipmentRepository.findAllByLineId(1L)).thenReturn(List.of(eq1, eq2));
            when(productionPerformanceRepository.findRecentByLineId(eq(line.getId()), any(Pageable.class)))
                .thenReturn(List.of(performance));

            // when
            GetProductionPlanEndTimeResponseDto response = productionPlanService.getProductionPlanEndTime(request);

            // then
            assertThat(response.getEndTime()).isEqualTo(testDateTime.plusMinutes(59));
        }

        @Test
        @DisplayName("라인 조회 실패")
        void testGetProductionPlanEndTime_noLine() {
            GetProductionPlanEndTimeRequestDto request = GetProductionPlanEndTimeRequestDto.builder()
                .lineCode("LINE-X")
                .plannedQty(BigDecimal.valueOf(100))
                .startTime(LocalDateTime.now())
                .build();

            when(lineRepository.findBylineCodeAndIsActiveTrue("LINE-X")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productionPlanService.getProductionPlanEndTime(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(LineErrorCode.LINE_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("설비 조회 실패")
        void testGetProductionPlanEndTime_noEquipment() {
            GetProductionPlanEndTimeRequestDto request = GetProductionPlanEndTimeRequestDto.builder()
                .lineCode("LINE-1")
                .plannedQty(BigDecimal.valueOf(100))
                .startTime(LocalDateTime.now())
                .build();

            when(lineRepository.findBylineCodeAndIsActiveTrue("LINE-1")).thenReturn(Optional.of(line));
            when(equipmentRepository.findAllByLineId(1L)).thenReturn(List.of());

            assertThatThrownBy(() -> productionPlanService.getProductionPlanEndTime(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(LineErrorCode.NO_EQUIPMENT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("생산계획 상태 변경 테스트")
    class UpdateProductionPlanStatusTest {

        @Test
        @DisplayName("생산계획 상태 변경 성공")
        void updateProductionPlanStatus_success() {
            // given
            List<Long> planIds = List.of(1L, 2L, 3L);
            PlanStatus newStatus = PlanStatus.COMPLETED;

            UpdateProductionPlanStatusRequestDto request =
                UpdateProductionPlanStatusRequestDto.builder()
                    .planIds(planIds)
                    .planStatus(newStatus)
                    .build();

            // update 성공: 영향 row 수 = 요청 ID 개수
            when(productionPlanRepository.updateAllStatusById(planIds, newStatus))
                .thenReturn(planIds.size());

            // 업데이트된 엔티티 조회
            List<ProductionPlans> plans = planIds.stream()
                .map(id -> ProductionPlans.builder()
                    .id(id)
                    .status(newStatus)
                    .build()
                ).toList();

            when(productionPlanRepository.findAllByIdIn(planIds)).thenReturn(plans);

            // when
            UpdateProductionPlanStatusResponseDto response =
                productionPlanService.updateProductionPlanStatus(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getPlanIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
            assertThat(response.getPlanStatus()).isEqualTo(newStatus);
        }

        @Test
        @DisplayName("생산계획 상태 변경 실패 - 영향된 row 수 미달")
        void updateProductionPlanStatus_failed() {
            // given
            List<Long> planIds = List.of(1L, 2L, 3L);
            PlanStatus newStatus = PlanStatus.COMPLETED;

            UpdateProductionPlanStatusRequestDto request =
                UpdateProductionPlanStatusRequestDto.builder()
                    .planIds(planIds)
                    .planStatus(newStatus)
                    .build();

            // update 실패 (예: 2개만 업데이트됨)
            when(productionPlanRepository.updateAllStatusById(planIds, newStatus))
                .thenReturn(2);

            // when & then
            assertThatThrownBy(() -> productionPlanService.updateProductionPlanStatus(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(CommonErrorCode.UNEXPECTED_ERROR.getMessage());
        }
    }

    @Nested
    @DisplayName("생산계획 삭제 테스트")
    class DeleteProductionPlanTest {

        private Users adminUser;
        private Users managerUser;
        private ProductionPlans planPending;

        @BeforeEach
        void setUp() {
            adminUser = productionManager.toBuilder()
                .id(1L)
                .role(Users.UserRole.ADMIN)
                .build();
            managerUser = productionManager.toBuilder()
                .id(2L)
                .role(Users.UserRole.MANAGER)
                .build();

            planPending = ProductionPlans.builder()
                .id(100L)
                .status(ProductionPlans.PlanStatus.PENDING)
                .productionManagerId(managerUser.getId())
                .build();
        }

        @Test
        @DisplayName("삭제 성공 - ADMIN 권한")
        void deleteProductionPlan_success_admin() {
            when(productionPlanRepository.findById(planPending.getId()))
                .thenReturn(Optional.of(planPending));

            productionPlanService.deleteProductionPlan(planPending.getId(), adminUser);

            verify(productionPlanRepository, times(1)).deleteById(planPending.getId());
        }

        @Test
        @DisplayName("삭제 성공 - MANAGER 권한, 담당자 일치")
        void deleteProductionPlan_success_manager() {
            when(productionPlanRepository.findById(planPending.getId()))
                .thenReturn(Optional.of(planPending));

            productionPlanService.deleteProductionPlan(planPending.getId(), managerUser);

            verify(productionPlanRepository, times(1)).deleteById(planPending.getId());
        }

        @Test
        @DisplayName("삭제 실패 - PLAN_NOT_FOUND")
        void deleteProductionPlan_fail_notFound() {
            when(productionPlanRepository.findById(999L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> productionPlanService.deleteProductionPlan(999L, adminUser))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("삭제 실패 - 상태 제한 (삭제 불가 상태)")
        void deleteProductionPlan_fail_forbiddenStatus() {
            Long planId = 200L;
            ProductionPlans plan = ProductionPlans.builder()
                .id(planId)
                .status(ProductionPlans.PlanStatus.COMPLETED)
                .productionManagerId(managerUser.getId())
                .build();

            when(productionPlanRepository.findById(plan.getId()))
                .thenReturn(Optional.of(plan));

            assertThatThrownBy(() -> productionPlanService.deleteProductionPlan(planId, adminUser))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_UPDATABLE.getMessage());
        }

        @Test
        @DisplayName("삭제 실패 - MANAGER 권한, 담당자 불일치")
        void deleteProductionPlan_fail_managerMismatch() {
            Long planId = planPending.getId();
            Users otherManager = Users.builder().id(999L).role(Users.UserRole.MANAGER).build();

            when(productionPlanRepository.findById(planId))
                .thenReturn(Optional.of(planPending));

            assertThatThrownBy(() -> productionPlanService.deleteProductionPlan(planId, otherManager))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN.getMessage());
        }
    }

    @Nested
    @DisplayName("생산계획 일괄 삭제 테스트")
    class DeleteProductionPlansTest {

        private Users adminUser;
        private Users managerUser;
        private ProductionPlans plan1;
        private ProductionPlans plan2;

        @BeforeEach
        void setUp() {
            adminUser = productionManager.toBuilder()
                .id(1L)
                .role(Users.UserRole.ADMIN)
                .build();

            managerUser = productionManager.toBuilder()
                .id(2L)
                .role(Users.UserRole.MANAGER)
                .build();

            plan1 = ProductionPlans.builder()
                .id(100L)
                .status(ProductionPlans.PlanStatus.PENDING)
                .productionManagerId(managerUser.getId())
                .build();

            plan2 = ProductionPlans.builder()
                .id(101L)
                .status(PlanStatus.PENDING)
                .productionManagerId(managerUser.getId())
                .build();
        }

        @Test
        @DisplayName("일괄 삭제 성공 - ADMIN 권한")
        void deleteProductionPlans_success_admin() {
            DeleteProductionPlanRequestDto request = DeleteProductionPlanRequestDto.builder()
                .planIds(List.of(plan1.getId(), plan2.getId()))
                .build();

            when(productionPlanRepository.findAllById(request.getPlanIds()))
                .thenReturn(List.of(plan1, plan2));

            productionPlanService.deleteProductionPlans(request, adminUser);

            verify(productionPlanRepository, times(1)).deleteAll(List.of(plan1, plan2));
        }

        @Test
        @DisplayName("일괄 삭제 성공 - MANAGER 권한, 담당 계획만")
        void deleteProductionPlans_success_manager() {
            List<Long> planIds = List.of(plan1.getId(), plan2.getId());

            DeleteProductionPlanRequestDto request = DeleteProductionPlanRequestDto.builder()
                .planIds(planIds)
                .build();

            when(productionPlanRepository.findAllById(planIds))
                .thenReturn(List.of(plan1, plan2));

            productionPlanService.deleteProductionPlans(request, managerUser);

            verify(productionPlanRepository, times(1)).deleteAll(List.of(plan1, plan2));
        }

        @Test
        @DisplayName("일괄 삭제 실패 - 존재하지 않는 계획 포함")
        void deleteProductionPlans_fail_notFound() {
            DeleteProductionPlanRequestDto request = DeleteProductionPlanRequestDto.builder()
                .planIds(List.of(999L))
                .build();

            when(productionPlanRepository.findAllById(List.of(999L)))
                .thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> productionPlanService.deleteProductionPlans(request, adminUser))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("일괄 삭제 실패 - MANAGER 권한, 담당자 불일치")
        void deleteProductionPlans_fail_managerMismatch() {
            DeleteProductionPlanRequestDto request = DeleteProductionPlanRequestDto.builder()
                .planIds(List.of(plan1.getId()))
                .build();

            Users otherManager = productionManager.toBuilder()
                .id(999L)
                .role(Users.UserRole.MANAGER)
                .build();

            when(productionPlanRepository.findAllById(List.of(plan1.getId()))).thenReturn(List.of(plan1));

            assertThatThrownBy(() -> productionPlanService.deleteProductionPlans(request, otherManager))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_FORBIDDEN.getMessage());
        }

        @Test
        @DisplayName("삭제 실패 - 상태 제한 (삭제 불가 상태)")
        void deleteProductionPlans_fail_forbiddenStatus() {
            ProductionPlans planRun = plan1.toBuilder()
                .status(PlanStatus.RUNNING)
                .build();

            List<Long> planIds = List.of(planRun.getId());
            DeleteProductionPlanRequestDto request = DeleteProductionPlanRequestDto.builder()
                .planIds(planIds)
                .build();

            when(productionPlanRepository.findAllById(planIds))
                .thenReturn(List.of(planRun));

            assertThatThrownBy(() -> productionPlanService.deleteProductionPlans(request, adminUser))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPlanErrorCode.PRODUCTION_PLAN_NOT_UPDATABLE.getMessage());
        }
    }
}
