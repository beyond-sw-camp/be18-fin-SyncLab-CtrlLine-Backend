package com.beyond.synclab.ctrlline.domain.defective.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveListResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveListRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectives;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.querydsl.jpa.impl.JPAQueryFactory;
import config.QuerydslTestConfig;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.data.domain.Sort;

@DataJpaTest
@AutoConfigureMockMvc(addFilters = false)
@Import(QuerydslTestConfig.class)
class PlanDefectiveQueryRepositoryImplTest {

    @Autowired
    private EntityManager em;

    private PlanDefectiveQueryRepositoryImpl queryRepository;

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

        // 테스트 환경에서는 엔티티 로깅 비활성화
        System.setProperty("logging.entity.enabled", "false");

        queryRepository = new PlanDefectiveQueryRepositoryImpl(new JPAQueryFactory(em));

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

        // ---------- 불량 데이터 ----------
        em.persist(PlanDefectives.builder()
            .defectiveDocumentNo("DEF-20251118-1")
            .productionPlanId(planA.getId())
            .productionPlan(planA)
            .createdAt(LocalDateTime.now().minusHours(4))
            .build());

        em.persist(PlanDefectives.builder()
            .defectiveDocumentNo("DEF-20251119-1")
            .productionPlanId(planB.getId())
            .productionPlan(planB)
            .createdAt(LocalDateTime.now().minusHours(2))
            .build());

        // ---------- 생산실적 ----------
        em.persist(ProductionPerformances.builder()
            .productionPlan(planA)
            .productionPlanId(planA.getId())
            .performanceDocumentNo("2025/11/18-1")
            .totalQty(BigDecimal.valueOf(100))
            .performanceQty(BigDecimal.valueOf(95))
            .performanceDefectiveRate(BigDecimal.valueOf(5))
            .startTime(LocalDateTime.now().minusHours(4))
            .endTime(LocalDateTime.now())
            .build());

        em.persist(ProductionPerformances.builder()
            .productionPlan(planB)
            .productionPlanId(planB.getId())
            .performanceDocumentNo("2025/11/19-1")
            .totalQty(BigDecimal.valueOf(200))
            .performanceQty(BigDecimal.valueOf(190))
            .performanceDefectiveRate(BigDecimal.valueOf(5))
            .startTime(LocalDateTime.now().minusHours(2))
            .endTime(LocalDateTime.now())
            .build());

        em.flush();
        em.clear();
    }

    // ================================================================
    // 1. 기본 조회 + 정렬 테스트
    // ================================================================
    @Test
    @DisplayName("기본 조회 - createdAt DESC 정렬 확인")
    void testFindDefectiveList_defaultSort() {

        SearchDefectiveListRequestDto request = SearchDefectiveListRequestDto.builder().build();
        Pageable pageable = PageRequest.of(0, 10);

        Page<GetDefectiveListResponseDto> result = queryRepository.findDefectiveList(request, pageable);
        assertThat(result.getTotalElements()).isEqualTo(2);

        assertThat(result.getContent().get(0).getDefectiveDocNo()).isEqualTo("DEF-20251119-1");
        assertThat(result.getContent().get(1).getDefectiveDocNo()).isEqualTo("DEF-20251118-1");
    }

    // ================================================================
    // 2. 조건 검색 테스트
    // ================================================================
    @Test
    @DisplayName("기간 + 문서번호 필터 조회 테스트")
    void testFindDefectiveList_withFilter() {

        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(1);

        SearchDefectiveListRequestDto request = SearchDefectiveListRequestDto.builder()
            .fromDate(from)
            .toDate(to)
            .productionPerformanceDocNo("2025/11/18-1")
            .build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<GetDefectiveListResponseDto> result = queryRepository.findDefectiveList(request, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDefectiveDocNo()).isEqualTo("DEF-20251118-1");
    }

    // ================================================================
    // 3. 정렬 테스트
    // ================================================================
    @Test
    @DisplayName("정렬 옵션 적용 테스트 - defectiveQty ASC")
    void testFindDefectiveList_sortByDefectiveQty() {

        SearchDefectiveListRequestDto request = SearchDefectiveListRequestDto.builder().build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "defectiveQty"));

        Page<GetDefectiveListResponseDto> result = queryRepository.findDefectiveList(request, pageable);

        assertThat(result.getContent().get(0).getDefectiveTotalQty())
            .isLessThanOrEqualTo(result.getContent().get(1).getDefectiveTotalQty());
    }
}