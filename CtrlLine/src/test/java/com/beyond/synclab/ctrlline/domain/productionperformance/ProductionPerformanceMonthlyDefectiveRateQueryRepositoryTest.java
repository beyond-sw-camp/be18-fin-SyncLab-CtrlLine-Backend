package com.beyond.synclab.ctrlline.domain.productionperformance;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceMonthlyDefRateQueryRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceMonthlyDefRateQueryRepositoryImpl;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslTestConfig.class, ProductionPerformanceMonthlyDefRateQueryRepositoryImpl.class})
class ProductionPerformanceMonthlyDefectiveRateQueryRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductionPerformanceMonthlyDefRateQueryRepositoryImpl defectiveRepo;

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
    // 1. 단일 월 total/performance SUM 테스트
    // =============================================================
    @Test
    @DisplayName("단일 월 totalQtySum / performanceQtySum 조회 정상 동작")
    void testSingleMonthQtySum() {

        Map<YearMonth, ProductionPerformanceMonthlyDefRateQueryRepository.MonthlyQtySum> result =
                defectiveRepo.getMonthlyQtySum(
                        "F001",
                        List.of(YearMonth.of(2025, 11))
                );

        ProductionPerformanceMonthlyDefRateQueryRepository.MonthlyQtySum sum = result.get(YearMonth.of(2025, 11));

        assertThat(sum.getTotalQtySum()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(sum.getPerformanceQtySum()).isEqualByComparingTo(BigDecimal.valueOf(90));
    }

    // =============================================================
    // 2. 6개월 SUM 조회 테스트 (기준월 포함 이전 5개월)
    // =============================================================
    @Test
    @DisplayName("최근 6개월 total/performance SUM 조회")
    void testSixMonthQtySum() {

        List<YearMonth> months = List.of(
                YearMonth.of(2025, 7),
                YearMonth.of(2025, 8),
                YearMonth.of(2025, 9),
                YearMonth.of(2025, 10),
                YearMonth.of(2025, 11),
                YearMonth.of(2025, 12)
        );

        Map<YearMonth, ProductionPerformanceMonthlyDefRateQueryRepository.MonthlyQtySum> result =
                defectiveRepo.getMonthlyQtySum("F001", months);

        assertThat(result)
                .satisfies(map -> {

                    assertThat(map.get(YearMonth.of(2025, 11)))
                            .extracting("totalQtySum", "performanceQtySum")
                            .containsExactly(
                                    new BigDecimal("100.00"),
                                    new BigDecimal("90.00")
                            );

                    assertThat(map.get(YearMonth.of(2025, 12)))
                            .extracting("totalQtySum", "performanceQtySum")
                            .containsExactly(
                                    new BigDecimal("200.00"),
                                    new BigDecimal("180.00")
                            );

                    assertThat(map)
                            .doesNotContainKeys(
                                    YearMonth.of(2025, 7),
                                    YearMonth.of(2025, 8),
                                    YearMonth.of(2025, 9),
                                    YearMonth.of(2025, 10)
                            );
                });
    }

    // =============================================================
    // 3. 데이터 없는 월 테스트
    // =============================================================
    @Test
    @DisplayName("데이터 없는 월은 결과에 포함되지 않는다")
    void testEmptyMonth() {

        Map<YearMonth, ProductionPerformanceMonthlyDefRateQueryRepository.MonthlyQtySum> result =
                defectiveRepo.getMonthlyQtySum(
                        "F001",
                        List.of(YearMonth.of(2025, 10))
                );

        assertThat(result.containsKey(YearMonth.of(2025, 10))).isFalse();
    }
}
