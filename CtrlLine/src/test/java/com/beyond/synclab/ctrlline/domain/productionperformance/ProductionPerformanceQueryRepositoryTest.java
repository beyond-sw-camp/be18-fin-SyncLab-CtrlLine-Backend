package com.beyond.synclab.ctrlline.domain.productionperformance;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceQueryRepositoryImpl;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class ProductionPerformanceQueryRepositoryTest {

    @Autowired
    private EntityManager em;

    private ProductionPerformanceQueryRepositoryImpl queryRepository;

    private Factories factory;
    private Lines line;
    private Items itemA;
    private Items itemB;
    private Users salesManager;
    private Users prodManager;

    @BeforeEach
    void setUp() {
        queryRepository = new ProductionPerformanceQueryRepositoryImpl(new com.querydsl.jpa.impl.JPAQueryFactory(em));

        // 공장
        factory = Factories.builder()
                .id(1L)
                .factoryCode("F001")
                .factoryName("테스트공장")
                .isActive(true)
                .build();
        em.persist(factory);

        // 라인
        line = Lines.builder()
                .id(10L)
                .factoryId(factory.getId())
                .lineCode("L01")
                .lineName("1라인")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        em.persist(line);

        // 품목
        itemA = Items.builder()
                .id(100L)
                .itemCode("ITEM-A")
                .itemName("제품A")
                .isActive(true)
                .build();
        em.persist(itemA);

        itemB = Items.builder()
                .id(200L)
                .itemCode("ITEM-B")
                .itemName("제품B")
                .isActive(true)
                .build();
        em.persist(itemB);

        // item-line 매핑
        em.persist(ItemsLines.builder().id(1L).line(line).item(itemA).build());
        em.persist(ItemsLines.builder().id(2L).line(line).item(itemB).build());

        // 사용자 (영업/생산)
        salesManager = Users.builder()
                .id(1000L)
                .empNo("E1000")
                .name("홍길동")
                .status(Users.UserStatus.ACTIVE)
                .role(Users.UserRole.USER)
                .position(Users.UserPosition.ASSISTANT)
                .build();
        em.persist(salesManager);

        prodManager = Users.builder()
                .id(2000L)
                .empNo("E2000")
                .name("김철수")
                .status(Users.UserStatus.ACTIVE)
                .role(Users.UserRole.USER)
                .position(Users.UserPosition.ASSISTANT_MANAGER)
                .build();
        em.persist(prodManager);

        // 생산계획
        ProductionPlans planA = ProductionPlans.builder()
                .id(300L)
                .documentNo("2025/11/18-1")
                .line(line)
                .salesManager(salesManager)
                .productionManager(prodManager)
                .dueDate(LocalDate.now().plusDays(1))
                .plannedQty(BigDecimal.valueOf(100))
                .startTime(LocalDateTime.now().minusHours(5))
                .endTime(LocalDateTime.now())
                .build();
        em.persist(planA);

        ProductionPlans planB = ProductionPlans.builder()
                .id(301L)
                .documentNo("2025/11/19-1")
                .line(line)
                .salesManager(salesManager)
                .productionManager(prodManager)
                .dueDate(LocalDate.now().plusDays(2))
                .plannedQty(BigDecimal.valueOf(200))
                .startTime(LocalDateTime.now().minusHours(3))
                .endTime(LocalDateTime.now())
                .build();
        em.persist(planB);

        // 생산실적
        em.persist(ProductionPerformances.builder()
                .id(900L)
                .performanceDocumentNo("2025/11/18-1")
                .productionPlan(planA)
                .totalQty(100.0)
                .performanceQty(95.0)
                .performanceDefectiveRate(5.0)
                .remark("테스트A")
                .startTime(LocalDateTime.now().minusHours(4))
                .endTime(LocalDateTime.now())
                .build());

        em.persist(ProductionPerformances.builder()
                .id(901L)
                .performanceDocumentNo("2025/11/19-1")
                .productionPlan(planB)
                .totalQty(200.0)
                .performanceQty(190.0)
                .performanceDefectiveRate(5.0)
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
                        .documentNo("18")   // 2025/11/18-1
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

        // ✅ SonarQube 권장 패턴 (List 자체를 검사)
        assertThat(result.getContent()).hasSize(1);

        // ✅ 총 건수는 size() 비검증이라 OK
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
                        .startDate(today.minusDays(2).toString())
                        .endDate(today.plusDays(1).toString())
                        .itemCode("ITEM-B")
                        .salesManagerName("홍길동")
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
                        .minTotalQty(150.0)
                        .maxTotalQty(250.0)
                        .minDefectRate(5.0)
                        .maxDefectRate(5.0)
                        .build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<GetProductionPerformanceListResponseDto> result =
                queryRepository.searchProductionPerformanceList(condition, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getPerformanceQty()).isEqualTo(190.0);
    }

}
