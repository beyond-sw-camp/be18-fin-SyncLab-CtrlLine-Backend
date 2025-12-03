package com.beyond.synclab.ctrlline.domain.productionperformance;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.exception.ProductionPerformanceNotFoundException;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.service.ProductionPerformanceServiceImpl;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionPerformanceServiceUpdateTest {

    @Mock
    private ProductionPerformanceRepository performanceRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private PlanDefectiveRepository planDefectiveRepository;

    @InjectMocks
    private ProductionPerformanceServiceImpl service;

    private ProductionPerformances perf;
    private ProductionPlans plan;
    private Lots lot;

    // ------------------------------------------------------------
    // 공통 엔티티 세팅
    // ------------------------------------------------------------
    @BeforeEach
    void setup() {

        Factories fac = Factories.builder()
                .factoryCode("F001")
                .factoryName("테스트공장")
                .isActive(true)
                .build();

        Lines line = Lines.builder()
                .factory(fac)
                .factoryId(1L)
                .lineCode("L01")
                .lineName("1라인")
                .isActive(true)
                .build();

        Items item = Items.builder()
                .itemCode("ITEM-A")
                .itemName("제품A")
                .itemSpecification("SPEC-A")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();

        ItemsLines itemLine = ItemsLines.builder()
                .line(line)
                .lineId(1L)
                .item(item)
                .itemId(1L)
                .build();

        Users sm = Users.builder()
                .empNo("202510001")
                .name("영업담당자A")
                .email("sales@test.com")
                .password("1234")
                .phoneNumber("010-1111-2222")
                .hiredDate(LocalDate.now())
                .role(Users.UserRole.USER)
                .status(Users.UserStatus.ACTIVE)
                .department("영업부")
                .position(Users.UserPosition.MANAGER)
                .address("서울시")
                .build();

        Users pm = Users.builder()
                .empNo("202510002")
                .name("생산담당자A")
                .email("prod@test.com")
                .password("1234")
                .phoneNumber("010-3333-4444")
                .hiredDate(LocalDate.now())
                .role(Users.UserRole.USER)
                .status(Users.UserStatus.ACTIVE)
                .department("생산부")
                .position(Users.UserPosition.MANAGER)
                .address("경기도")
                .build();

        plan = ProductionPlans.builder()
                .id(10L)
                .itemLine(itemLine)
                .itemLineId(1L)
                .salesManager(sm)
                .salesManagerId(1L)
                .productionManager(pm)
                .productionManagerId(2L)
                .documentNo("PLAN-1")
                .plannedQty(BigDecimal.valueOf(100))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .dueDate(LocalDate.now().plusDays(3))
                .remark("old-plan")
                .build();

        perf = ProductionPerformances.builder()
                .id(1L)
                .productionPlan(plan)
                .productionPlanId(plan.getId())
                .performanceDocumentNo("2025/11/26-1")
                .totalQty(BigDecimal.valueOf(100))
                .performanceQty(BigDecimal.valueOf(95))
                .performanceDefectiveRate(BigDecimal.valueOf(5))
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now())
                .remark("OLD")
                .build();

        lot = Lots.builder()
                .lotNo("LOT-A001")
                .productionPlanId(plan.getId())
                .itemId(item.getId())
                .build();
    }

    // ------------------------------------------------------------
    // 1. 정상 수정
    // ------------------------------------------------------------
    @Test
    @DisplayName("remark 정상 수정")
    void updateRemark_success() {

        when(performanceRepository.findById(1L)).thenReturn(Optional.of(perf));
        when(lotRepository.findByProductionPlanId(plan.getId())).thenReturn(Optional.of(lot));
        when(planDefectiveRepository.findByProductionPlanId(plan.getId()))
                .thenReturn(Optional.empty());

        GetProductionPerformanceDetailResponseDto result =
                service.updatePerformanceRemark(1L, "NEW");

        assertThat(result.getRemark()).isEqualTo("NEW");
        verify(performanceRepository, times(2)).findById(1L);
    }

    // ------------------------------------------------------------
    // 2. remark = null → 기존값 유지
    // ------------------------------------------------------------
    @Test
    @DisplayName("remark null → 기존 remark 유지")
    void updateRemark_null_keepOld() {

        when(performanceRepository.findById(1L)).thenReturn(Optional.of(perf));
        when(lotRepository.findByProductionPlanId(plan.getId())).thenReturn(Optional.of(lot));
        when(planDefectiveRepository.findByProductionPlanId(plan.getId()))
                .thenReturn(Optional.empty());

        GetProductionPerformanceDetailResponseDto result =
                service.updatePerformanceRemark(1L, null);

        assertThat(result.getRemark()).isEqualTo("OLD");
    }

    // ------------------------------------------------------------
    // 3. remark = "" → 빈문자로 변경
    // ------------------------------------------------------------
    @Test
    @DisplayName("remark 빈 문자열 → 빈문자로 수정됨")
    void updateRemark_blank_update() {

        when(performanceRepository.findById(1L)).thenReturn(Optional.of(perf));
        when(lotRepository.findByProductionPlanId(plan.getId())).thenReturn(Optional.of(lot));
        when(planDefectiveRepository.findByProductionPlanId(plan.getId()))
                .thenReturn(Optional.empty());

        GetProductionPerformanceDetailResponseDto result =
                service.updatePerformanceRemark(1L, "");

        assertThat(result.getRemark()).isEmpty();
    }

    // ------------------------------------------------------------
    // 4. 없는 ID → 예외 발생
    // ------------------------------------------------------------
    @Test
    @DisplayName("없는 ID → ProductionPerformanceNotFoundException 발생")
    void updateRemark_notFound() {

        when(performanceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePerformanceRemark(999L, "TEST"))
                .isInstanceOf(ProductionPerformanceNotFoundException.class);
    }
}
