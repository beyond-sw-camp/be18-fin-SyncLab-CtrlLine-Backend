package com.beyond.synclab.ctrlline.domain.productionperformance;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceMonthlyQueryRepositoryImpl;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import config.QuerydslTestConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslTestConfig.class, ProductionPerformanceMonthlyQueryRepositoryImpl.class})
class ProductionPerformanceMonthlyQueryRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductionPerformanceMonthlyQueryRepositoryImpl monthlyRepository;

    private Factories factory;
    private Lines line;
    private Items itemA;
    private Items itemB;
    private ItemsLines itemLineA;
    private ItemsLines itemLineB;
    private Users salesManager;
    private Users prodManager;

    @BeforeEach
    void setUp() {

        // ---------- Factory ----------
        factory = Factories.builder()
                .factoryCode("F001")
                .factoryName("테스트공장")
                .isActive(true)
                .build();
        em.persist(factory);

        // ---------- Line ----------
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

        // ---------- Items ----------
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

        // ---------- Item-Lines ----------
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

        // ---------- Users ----------
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
                .address("서울시")
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
                .address("경기도")
                .build();
        em.persist(prodManager);

        // ---------- ProductionPlans ----------
        ProductionPlans planA = ProductionPlans.builder()
                .documentNo("2025/11/18-1")
                .itemLine(itemLineA)
                .itemLineId(itemLineA.getId())
                .salesManager(salesManager)
                .salesManagerId(salesManager.getId())
                .productionManager(prodManager)
                .productionManagerId(prodManager.getId())
                .dueDate(LocalDate.now())
                .plannedQty(BigDecimal.valueOf(100))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();
        em.persist(planA);

        ProductionPlans planB = ProductionPlans.builder()
                .documentNo("2025/12/05-1")
                .itemLine(itemLineB)
                .itemLineId(itemLineB.getId())
                .salesManager(salesManager)
                .salesManagerId(salesManager.getId())
                .productionManager(prodManager)
                .productionManagerId(prodManager.getId())
                .dueDate(LocalDate.now())
                .plannedQty(BigDecimal.valueOf(200))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();
        em.persist(planB);

        // ---------- ProductionPerformances ----------
        em.persist(ProductionPerformances.builder()
                .productionPlan(planA)
                .productionPlanId(planA.getId())
                .performanceDocumentNo("2025/11/18-1")
                .totalQty(BigDecimal.valueOf(100))
                .performanceQty(BigDecimal.valueOf(90))
                .performanceDefectiveRate(BigDecimal.valueOf(10))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .remark("test-A")
                .build());

        em.persist(ProductionPerformances.builder()
                .productionPlan(planB)
                .productionPlanId(planB.getId())
                .performanceDocumentNo("2025/12/05-1")
                .totalQty(BigDecimal.valueOf(200))
                .performanceQty(BigDecimal.valueOf(180))
                .performanceDefectiveRate(BigDecimal.valueOf(10))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .remark("test-B")
                .build());

        em.flush();
        em.clear();
    }

    // =============================================================
    // 1. 단일 월 SUM 테스트
    // =============================================================
    @Test
    @DisplayName("단일 월 생산량 SUM 정상 조회")
    void testMonthlySum_singleMonth() {

        Map<YearMonth, Long> result =
                monthlyRepository.getMonthlySum(
                        "F001",
                        java.util.List.of(
                                YearMonth.of(2025, 11)
                        )
                );

        assertThat(result).containsEntry(YearMonth.of(2025, 11), 90L);
    }

    // =============================================================
    // 2. 다중 월 SUM 테스트
    // =============================================================
    @Test
    @DisplayName("여러 개월 SUM 조회 - 누적 결과 테스트")
    void testMonthlySum_multipleMonths() {

        Map<YearMonth, Long> result =
                monthlyRepository.getMonthlySum(
                        "F001",
                        java.util.List.of(
                                YearMonth.of(2025, 11),
                                YearMonth.of(2025, 12)
                        )
                );

        assertThat(result)
                .containsAllEntriesOf(
                        Map.of(
                                YearMonth.of(2025, 11), 90L,
                                YearMonth.of(2025, 12), 180L
                        )
                );
    }

    // =============================================================
    // 3. 데이터 없는 월 테스트
    // =============================================================
    @Test
    @DisplayName("데이터 없는 월은 null 또는 missing")
    void testMonthlySum_emptyMonth() {

        Map<YearMonth, Long> result =
                monthlyRepository.getMonthlySum(
                        "F001",
                        java.util.List.of(
                                YearMonth.of(2025, 10)     // 데이터 없음
                        )
                );

        assertThat(result.containsKey(YearMonth.of(2025, 10))).isFalse();
    }
}
