package com.beyond.synclab.ctrlline.domain.productionperformance;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceQueryRepositoryImpl;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.querydsl.jpa.impl.JPAQueryFactory;
import config.QuerydslTestConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureMockMvc(addFilters = false)
@Import(QuerydslTestConfig.class)
class ProductionPerformanceQueryRepositoryTest {

    @Autowired
    private EntityManager em;

    private ProductionPerformanceQueryRepositoryImpl queryRepository;

    private Factories factory;
    private Lines line;
    private Items itemA;
    private Items itemB;
    private ItemsLines itemLineA;   // ✨ 추가됨
    private ItemsLines itemLineB;   // ✨ 추가됨
    private Users salesManager;
    private Users prodManager;

    @BeforeEach
    void setUp() {

        // 테스트 환경에서는 엔티티 로깅 비활성화
        System.setProperty("logging.entity.enabled", "false");

        // Querydsl Repository 초기화
        queryRepository = new ProductionPerformanceQueryRepositoryImpl(new JPAQueryFactory(em));

        // ---------- 공장 ----------
        factory = Factories.builder()
                .factoryCode("F001")
                .factoryName("테스트공장")
                .isActive(true)
                .build();
        em.persist(factory);

        // ---------- 라인 ----------
        line = Lines.builder()
                .factory(factory)
                .factoryId(factory.getId())
                .lineCode("L01")
                .lineName("1라인")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        em.persist(line);

        // ---------- 품목 ----------
        itemA = Items.builder()
                .itemCode("ITEM-A")
                .itemName("제품A")
                .itemStatus(ItemStatus.RAW_MATERIAL)
                .itemUnit("EA")
                .isActive(true)
                .build();
        em.persist(itemA);

        itemB = Items.builder()
                .itemCode("ITEM-B")
                .itemName("제품B")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .itemUnit("EA")
                .isActive(true)
                .build();
        em.persist(itemB);

        // ---------- Item-Line ----------
        itemLineA = ItemsLines.builder()
                .line(line)
                .lineId(line.getId())
                .item(itemA)
                .itemId(itemA.getId())
                .build();
        em.persist(itemLineA);

        itemLineB = ItemsLines.builder()
                .line(line)
                .lineId(line.getId())
                .item(itemB)
                .itemId(itemB.getId())
                .build();
        em.persist(itemLineB);

        // ---------- 사용자 ----------
        salesManager = Users.builder()
                .empNo("E1000")
                .name("홍길동")
                .email("hong@test.com")
                .password("1234")
                .phoneNumber("010-1111-2222")
                .hiredDate(LocalDate.now())
                .role(Users.UserRole.USER)
                .status(Users.UserStatus.ACTIVE)
                .department("영업부")
                .position(Users.UserPosition.ASSISTANT)
                .address("서울시 송파구")
                .build();
        em.persist(salesManager);

        prodManager = Users.builder()
                .empNo("E2000")
                .name("김철수")
                .email("kim@test.com")
                .password("1234")
                .phoneNumber("010-3333-4444")
                .hiredDate(LocalDate.now())
                .role(Users.UserRole.USER)
                .status(Users.UserStatus.ACTIVE)
                .department("생산부")
                .position(Users.UserPosition.ASSISTANT_MANAGER)
                .address("경기도 수원시")
                .build();
        em.persist(prodManager);

        // ---------- 생산계획 ----------
        ProductionPlans planA = ProductionPlans.builder()
                .documentNo("2025/11/18-1")
                .itemLine(itemLineA)
                .itemLineId(itemLineA.getId())
                .salesManager(salesManager)
                .salesManagerId(salesManager.getId())
                .productionManager(prodManager)
                .productionManagerId(prodManager.getId())
                .dueDate(LocalDate.now().plusDays(1))
                .plannedQty(BigDecimal.valueOf(100))
                .startTime(LocalDateTime.now().minusHours(5))
                .endTime(LocalDateTime.now())
                .build();
        em.persist(planA);

        ProductionPlans planB = ProductionPlans.builder()
                .documentNo("2025/11/19-1")
                .itemLine(itemLineB)
                .itemLineId(itemLineB.getId())
                .salesManager(salesManager)
                .salesManagerId(salesManager.getId())
                .productionManager(prodManager)
                .productionManagerId(prodManager.getId())
                .dueDate(LocalDate.now().plusDays(2))
                .plannedQty(BigDecimal.valueOf(200))
                .startTime(LocalDateTime.now().minusHours(3))
                .endTime(LocalDateTime.now())
                .build();
        em.persist(planB);

        // ---------- 생산실적 ----------
        em.persist(ProductionPerformances.builder()
                .productionPlan(planA)
                .productionPlanId(planA.getId())
                .performanceDocumentNo("2025/11/18-1")
                .totalQty(BigDecimal.valueOf(100))
                .performanceQty(BigDecimal.valueOf(95))
                .performanceDefectiveRate(BigDecimal.valueOf(5))
                .startTime(LocalDateTime.now().minusHours(4))
                .remark("테스트A")
                .endTime(LocalDateTime.now())
                .build());

        em.persist(ProductionPerformances.builder()
                .performanceDocumentNo("2025/11/19-1")
                .productionPlan(planB)
                .productionPlanId(planB.getId())
                .totalQty(BigDecimal.valueOf(200))
                .performanceQty(BigDecimal.valueOf(190))
                .performanceDefectiveRate(BigDecimal.valueOf(5))
                .remark("테스트B")
                .startTime(LocalDateTime.now().minusHours(2))
                .endTime(LocalDateTime.now())
                .build());

        em.flush();
        em.clear();
    }


    // =============================================================
    // 1. 전체 조회 + 기본 정렬 검증
    // =============================================================
    @Test
    @DisplayName("기본 조회 - performanceDocumentNo DESC 정렬 확인")
    void testSearch_defaultSort() {

        SearchProductionPerformanceRequestDto condition =
                SearchProductionPerformanceRequestDto.builder().build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<GetProductionPerformanceListResponseDto> result =
                queryRepository.searchProductionPerformanceList(condition, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getDocumentNo()).isEqualTo("2025/11/19-1"); // 최신 전표
        assertThat(result.getContent().get(1).getDocumentNo()).isEqualTo("2025/11/18-1");
    }

    // =============================================================
    // 2. documentNo 조건 조회
    // =============================================================
    @Test
    @DisplayName("documentNo contains 검색 정상 동작")
    void testSearch_documentNoFilter() {
        SearchProductionPerformanceRequestDto condition =
                SearchProductionPerformanceRequestDto.builder()
                        .documentDateFrom("2025/11/18")
                        .documentDateTo("2025/11/18")
                        .build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<GetProductionPerformanceListResponseDto> result =
                queryRepository.searchProductionPerformanceList(condition, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDocumentNo()).isEqualTo("2025/11/18-1");
    }

    // =============================================================
    // 3. 공장코드 조건 조회
    // =============================================================
    @Test
    @DisplayName("factoryCode 검색 정상 작동")
    void testSearch_factoryCode() {
        SearchProductionPerformanceRequestDto condition =
                SearchProductionPerformanceRequestDto.builder()
                        .factoryCode("F001")
                        .build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<GetProductionPerformanceListResponseDto> result =
                queryRepository.searchProductionPerformanceList(condition, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    // =============================================================
    // 4. 페이징 테스트
    // =============================================================
    @Test
    @DisplayName("페이징 동작 확인(size=1)")
    void testSearch_paging() {

        Pageable pageable = PageRequest.of(0, 1); // 한 건만 조회

        Page<GetProductionPerformanceListResponseDto> result =
                queryRepository.searchProductionPerformanceList(
                        SearchProductionPerformanceRequestDto.builder().build(), pageable
                );

        // SonarQube 권장 패턴 (List 자체를 검사)
        assertThat(result.getContent()).hasSize(1);

        // 총 건수는 size() 비검증이라 OK
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    // =============================================================
    // 5. 공장 + 라인 + 품목 다중 조건 테스트
    // =============================================================
    @Test
    @DisplayName("공장 + 라인 + 품목 다중 조건 검색")
    void testSearch_multiCondition_factory_line_item() {

        SearchProductionPerformanceRequestDto condition =
                SearchProductionPerformanceRequestDto.builder()
                        .factoryCode("F001")
                        .lineCode("L01")
                        .itemCode("ITEM-A")
                        .build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<GetProductionPerformanceListResponseDto> result =
                queryRepository.searchProductionPerformanceList(condition, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getItemCode()).isEqualTo("ITEM-A");
    }

    // =============================================================
    // 6. 날짜 범위 + 품목 + 영업담당자 다중 조건 테스트
    // =============================================================
    @Test
    @DisplayName("기간 + 품목 + 영업담당자 다중 조건 검색")
    void testSearch_multiCondition_date_item_salesManager() {

        LocalDate today = LocalDate.now();

        SearchProductionPerformanceRequestDto condition =
                SearchProductionPerformanceRequestDto.builder()
                        .startTimeFrom(today.minusDays(2).toString())
                        .startTimeTo(today.plusDays(1).toString())
                        .itemCode("ITEM-B")
                        .salesManagerNo("E1000")
                        .build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<GetProductionPerformanceListResponseDto> result =
                queryRepository.searchProductionPerformanceList(condition, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getItemCode()).isEqualTo("ITEM-B");
        assertThat(result.getContent().get(0).getSalesManagerNo()).isEqualTo("E1000");
    }

    // =============================================================
    // 7. 수량범위 + 불량률범위 다중 조건 테스트
    // =============================================================
    @Test
    @DisplayName("수량범위 + 불량률 범위 다중 조건 검색")
    void testSearch_multiCondition_qty_defectRate() {

        SearchProductionPerformanceRequestDto condition =
                SearchProductionPerformanceRequestDto.builder()
                        .minTotalQty(BigDecimal.valueOf(150))
                        .maxTotalQty(BigDecimal.valueOf(250))
                        .minDefectRate(BigDecimal.valueOf(5))
                        .maxDefectRate(BigDecimal.valueOf(5))
                        .build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<GetProductionPerformanceListResponseDto> result =
                queryRepository.searchProductionPerformanceList(condition, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getPerformanceQty())
                .isEqualByComparingTo("190");
    }
}
