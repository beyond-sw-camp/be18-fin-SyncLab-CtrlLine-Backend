package com.beyond.synclab.ctrlline.domain.lot;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.lot.dto.request.SearchLotRequestDto;
import com.beyond.synclab.ctrlline.domain.lot.dto.response.GetLotListResponseDto;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.repository.query.LotQueryRepositoryImpl;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
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
class LotQueryRepositoryTest {

    @Autowired
    private EntityManager em;

    private LotQueryRepositoryImpl queryRepository;

    private Factories factory;
    private Lines line;
    private Items itemA;
    private Items itemB;
    private ItemsLines itemLineA;
    private ItemsLines itemLineB;
    private Users prodManager;

    private ProductionPlans planA;
    private ProductionPlans planB;

    @BeforeEach
    void setUp() {
        System.setProperty("logging.entity.enabled", "false");

        queryRepository = new LotQueryRepositoryImpl(new JPAQueryFactory(em));

        // ===== 공장 =====
        factory = Factories.builder()
                .factoryCode("F001")
                .factoryName("테스트공장")
                .isActive(true)
                .build();
        em.persist(factory);

        // ===== 라인 =====
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

        // ===== 품목 =====
        itemA = Items.builder()
                .itemCode("ITEM-A")
                .itemName("제품A")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .itemUnit("EA")
                .build();
        em.persist(itemA);

        itemB = Items.builder()
                .itemCode("ITEM-B")
                .itemName("제품B")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .itemUnit("EA")
                .build();
        em.persist(itemB);

        // ===== Item-Line =====
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

        // ===== 생산담당자 =====
        prodManager = Users.builder()
                .empNo("E9000")
                .name("김담당")
                .email("pro@test.com")
                .password("1234")
                .phoneNumber("010-5555-6666")
                .hiredDate(LocalDate.now())
                .role(Users.UserRole.MANAGER)
                .status(Users.UserStatus.ACTIVE)
                .department("생산부")
                .position(Users.UserPosition.MANAGER)
                .address("경기도 용인시")
                .build();
        em.persist(prodManager);

        // ===== 생산계획 =====
        planA = ProductionPlans.builder()
                .documentNo("2025/11/18-1")
                .itemLine(itemLineA)
                .itemLineId(itemLineA.getId())
                .productionManager(prodManager)
                .productionManagerId(prodManager.getId())
                .plannedQty(BigDecimal.valueOf(100))
                .dueDate(LocalDate.now().plusDays(1))
                .startTime(LocalDateTime.now().minusHours(6))
                .endTime(LocalDateTime.now().minusHours(3))
                .build();
        em.persist(planA);

        planB = ProductionPlans.builder()
                .documentNo("2025/11/19-1")
                .itemLine(itemLineB)
                .itemLineId(itemLineB.getId())
                .productionManager(prodManager)
                .productionManagerId(prodManager.getId())
                .plannedQty(BigDecimal.valueOf(200))
                .dueDate(LocalDate.now().plusDays(2))
                .startTime(LocalDateTime.now().minusHours(5))
                .endTime(LocalDateTime.now().minusHours(2))
                .build();
        em.persist(planB);

        // ===== 생산실적 =====
        em.persist(ProductionPerformances.builder()
                .productionPlan(planA)
                .productionPlanId(planA.getId())
                .performanceDocumentNo("2025/11/18-1")
                .totalQty(BigDecimal.valueOf(100))
                .performanceQty(BigDecimal.valueOf(90))
                .performanceDefectiveRate(BigDecimal.valueOf(10))
                .startTime(LocalDateTime.now().minusHours(5))
                .endTime(LocalDateTime.now().minusHours(4))
                .remark("LOT-A")
                .build());

        em.persist(ProductionPerformances.builder()
                .productionPlan(planB)
                .productionPlanId(planB.getId())
                .performanceDocumentNo("2025/11/19-1")
                .totalQty(BigDecimal.valueOf(200))
                .performanceQty(BigDecimal.valueOf(180))
                .performanceDefectiveRate(BigDecimal.valueOf(10))
                .startTime(LocalDateTime.now().minusHours(4))
                .endTime(LocalDateTime.now().minusHours(3))
                .remark("LOT-B")
                .build());

        // ===== LOT 생성 =====
        em.persist(Lots.builder()
                .productionPlanId(planA.getId())
                .itemId(itemA.getId())
                .lotNo("2025/11/18-1")
                .build());

        em.persist(Lots.builder()
                .productionPlanId(planB.getId())
                .itemId(itemB.getId())
                .lotNo("2025/11/19-1")
                .build());

        em.flush();
        em.clear();
    }

    // =============================================================
    // 1. 기본조회 + 정렬 검증
    // =============================================================
    @Test
    @DisplayName("기본 조회 - lotNo DESC 정렬")
    void testLot_defaultSort() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<GetLotListResponseDto> result =
                queryRepository.searchLotList(SearchLotRequestDto.builder().build(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getLotNo()).isEqualTo("2025/11/19-1");
        assertThat(result.getContent().get(1).getLotNo()).isEqualTo("2025/11/18-1");
    }

    // =============================================================
    // 2. lotNo filtering
    // =============================================================
    @Test
    @DisplayName("lotNo contains 검색 정상 동작")
    void testLot_lotNoFilter() {

        SearchLotRequestDto condition = SearchLotRequestDto.builder()
                .lotNo("18")
                .build();

        Page<GetLotListResponseDto> result =
                queryRepository.searchLotList(condition, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getLotNo()).isEqualTo("2025/11/18-1");
    }

    // =============================================================
    // 3. factoryCode filtering
    // =============================================================
    @Test
    @DisplayName("factoryCode 검색 정상 작동")
    void testLot_factoryCode() {

        SearchLotRequestDto condition = SearchLotRequestDto.builder()
                .factoryCode("F001")
                .build();

        Page<GetLotListResponseDto> result =
                queryRepository.searchLotList(condition, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    // =============================================================
    // 4. 페이징 확인
    // =============================================================
    @Test
    @DisplayName("페이징 동작(size=1)")
    void testLot_paging() {

        Page<GetLotListResponseDto> result =
                queryRepository.searchLotList(
                        SearchLotRequestDto.builder().build(),
                        PageRequest.of(0, 1)
                );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    // =============================================================
    // 5. 공장 + 라인 + 품목 다중 조건 테스트
    // =============================================================
    @Test
    @DisplayName("공장 + 라인 + 품목 다중 조건 검색")
    void testLot_multiCondition_factory_line_item() {

        SearchLotRequestDto condition = SearchLotRequestDto.builder()
                .factoryCode("F001")
                .lineCode("L01")
                .itemCode("ITEM-A")
                .build();

        Page<GetLotListResponseDto> result =
                queryRepository.searchLotList(condition, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getItemCode()).isEqualTo("ITEM-A");
    }
}