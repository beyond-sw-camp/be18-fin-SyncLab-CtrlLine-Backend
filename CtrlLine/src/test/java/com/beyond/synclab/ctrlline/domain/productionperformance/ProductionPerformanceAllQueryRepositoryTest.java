package com.beyond.synclab.ctrlline.domain.productionperformance;

import com.beyond.synclab.ctrlline.config.QuerydslConfig;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.itemline.repository.ItemLineRepository;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchAllProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetAllProductionPerformanceResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceAllQueryRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceAllQueryRepositoryImpl;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureMockMvc(addFilters = false)
@Import({
        QuerydslConfig.class,
        ProductionPerformanceAllQueryRepositoryImpl.class
})
class ProductionPerformanceAllQueryRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    ProductionPerformanceAllQueryRepository queryRepository;
    @Autowired
    ProductionPerformanceRepository performanceRepository;
    @Autowired
    ProductionPlanRepository planRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    ItemLineRepository itemLineRepository;
    @Autowired
    LineRepository lineRepository;
    @Autowired
    FactoryRepository factoryRepository;
    @Autowired
    LotRepository lotRepository;
    @Autowired
    UserRepository userRepository;

    ProductionPlans planA;
    ProductionPlans planB;

    @BeforeEach
    void setup() {

        // ===== Factory =====
        Factories factory = factoryRepository.save(
                Factories.builder()
                        .factoryCode("F0001")
                        .factoryName("공장1")
                        .isActive(true)
                        .build()
        );

        // ===== Line =====
        Lines line = lineRepository.save(
                Lines.builder()
                        .lineCode("L0001")
                        .lineName("1라인")
                        .factoryId(factory.getId())
                        .isActive(true)
                        .build()
        );

        // ===== Item =====
        Items item = itemRepository.save(
                Items.builder()
                        .itemCode("ITEM-A")
                        .itemName("제품A")
                        .itemSpecification("SPEC-A")
                        .itemUnit("EA")
                        .itemStatus(ItemStatus.FINISHED_PRODUCT)
                        .isActive(true)
                        .build()
        );

        // ===== ItemLine =====
        ItemsLines itemLine = itemLineRepository.save(
                ItemsLines.builder().item(item).line(line).build()
        );

        Users sm = userRepository.save(
                Users.builder()
                        .empNo("202510001")
                        .name("영업담당자A")
                        .email("salesA@test.com")
                        .password("test1234")
                        .phoneNumber("010-1111-2222")
                        .hiredDate(LocalDate.now())
                        .role(Users.UserRole.USER)
                        .status(Users.UserStatus.ACTIVE)
                        .department("영업부")
                        .position(Users.UserPosition.MANAGER)
                        .address("서울특별시 테스트구 123")
                        .build()
        );

        Users pm = userRepository.save(
                Users.builder()
                        .empNo("202510002")
                        .name("생산담당자A")
                        .email("prodA@test.com")
                        .password("test1234")
                        .phoneNumber("010-3333-4444")
                        .hiredDate(LocalDate.now())
                        .role(Users.UserRole.USER)
                        .status(Users.UserStatus.ACTIVE)
                        .department("생산부")
                        .position(Users.UserPosition.MANAGER)
                        .address("경기도 테스트시 456")
                        .build()
        );

        // ===== Plan A =====
        planA = planRepository.save(
                ProductionPlans.builder()
                        .documentNo("PLAN-A-001")
                        .itemLineId(itemLine.getId())
                        .itemLine(itemLine)
                        .salesManagerId(sm.getId())
                        .productionManagerId(pm.getId())
                        .plannedQty(BigDecimal.valueOf(100))
                        .startTime(LocalDateTime.of(2025, 1, 10, 12, 0))
                        .endTime(LocalDateTime.of(2025, 1, 10, 18, 0))
                        .dueDate(LocalDate.now().plusDays(2))
                        .remark("테스트용 생산계획 A")
                        .build()
        );

        // ===== Plan B =====
        planB = planRepository.save(
                ProductionPlans.builder()
                        .documentNo("PLAN-B-001")
                        .itemLineId(itemLine.getId())
                        .itemLine(itemLine)
                        .salesManagerId(sm.getId())
                        .productionManagerId(pm.getId())
                        .plannedQty(BigDecimal.valueOf(200))
                        .startTime(LocalDateTime.of(2025, 2, 10, 12, 0))
                        .endTime(LocalDateTime.of(2025, 2, 10, 18, 0))
                        .dueDate(LocalDate.now().plusDays(1))
                        .remark("테스트용 생산계획 B")
                        .build()
        );

        // ===== LOT 등록 =====
        lotRepository.save(
                Lots.builder()
                        .lotNo("LOT-A001")
                        .productionPlanId(planA.getId())
                        .itemId(planA.getItemLine().getItem().getId())
                        .build()
        );

        lotRepository.save(
                Lots.builder()
                        .lotNo("LOT-B001")
                        .productionPlanId(planB.getId())
                        .itemId(planB.getItemLine().getItem().getId())
                        .build()
        );

        // ===== Performance A =====
        performanceRepository.save(
                ProductionPerformances.builder()
                        .productionPlan(planA)
                        .productionPlanId(planA.getId())
                        .performanceDocumentNo("2025/11/26-1")
                        .totalQty(BigDecimal.valueOf(100))
                        .performanceQty(BigDecimal.valueOf(95))
                        .performanceDefectiveRate(BigDecimal.valueOf(5))
                        .startTime(LocalDateTime.now().minusHours(3))
                        .endTime(LocalDateTime.now().minusHours(1))
                        .remark("테스트A")
                        .build()
        );

        // ===== Performance B =====
        performanceRepository.save(
                ProductionPerformances.builder()
                        .productionPlan(planB)
                        .productionPlanId(planB.getId())
                        .performanceDocumentNo("2025/11/25-1")
                        .totalQty(BigDecimal.valueOf(120))
                        .performanceQty(BigDecimal.valueOf(100))
                        .performanceDefectiveRate(BigDecimal.valueOf(20))
                        .startTime(LocalDateTime.now().minusHours(4))
                        .endTime(LocalDateTime.now().minusHours(2))
                        .remark("테스트B")
                        .build()
        );

        em.flush();
        em.clear();
    }

    // =======================================================================
    // 테스트
    // =======================================================================

    @Test
    @DisplayName("현황조회 - 전체 조회 성공")
    void testSearchAll_basic() {

        SearchAllProductionPerformanceRequestDto condition =
                SearchAllProductionPerformanceRequestDto.builder().build();

        List<GetAllProductionPerformanceResponseDto> result =
                queryRepository.searchAll(condition);

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("현황조회 - 수량 범위 조건(totalQty) 정상 적용")
    void testSearchAll_filterByTotalQty() {

        SearchAllProductionPerformanceRequestDto condition =
                SearchAllProductionPerformanceRequestDto.builder()
                        .minTotalQty(BigDecimal.valueOf(110))
                        .maxTotalQty(BigDecimal.valueOf(130))
                        .build();

        List<GetAllProductionPerformanceResponseDto> result =
                queryRepository.searchAll(condition);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getTotalQty()).isEqualTo("120.00");
    }

    @Test
    @DisplayName("현황조회 - 조건에 맞는 데이터가 없으면 빈 결과 반환")
    void testSearchAll_noResult() {

        SearchAllProductionPerformanceRequestDto condition =
                SearchAllProductionPerformanceRequestDto.builder()
                        .factoryCode("NO_SUCH_FACTORY")
                        .build();

        List<GetAllProductionPerformanceResponseDto> result =
                queryRepository.searchAll(condition);

        assertThat(result).isEmpty();
    }
}
