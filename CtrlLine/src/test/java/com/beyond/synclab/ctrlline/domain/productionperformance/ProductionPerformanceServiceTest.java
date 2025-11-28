package com.beyond.synclab.ctrlline.domain.productionperformance;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.service.LotService;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.exception.ProductionPerformanceErrorCode;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.service.ProductionPerformanceServiceImpl;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionPerformanceServiceTest {

    @Mock
    private ProductionPerformanceRepository performanceRepository;

    @Mock
    private LotService lotService;

    @InjectMocks
    private ProductionPerformanceServiceImpl productionPerformanceService;

    // -------------------------------------------------------------
    // 공통 엔티티 생성 메서드
    // -------------------------------------------------------------
    private Users user(String empNo) {
        return Users.builder()
                .empNo(empNo)
                .name("테스트유저")
                .email(empNo + "@test.com")
                .password("1234")
                .phoneNumber("010-1111-2222")
                .hiredDate(LocalDate.now())
                .role(Users.UserRole.USER)
                .status(Users.UserStatus.ACTIVE)
                .department("테스트부서")
                .position(Users.UserPosition.ASSISTANT)
                .address("서울시")
                .build();
    }

    private Items item() {
        return Items.builder()
                .itemCode("ITEM001")
                .itemName("테스트품목")
                .itemSpecification("SPEC")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();
    }

    private Lines line() {
        Factories factory = Factories.builder()
                .factoryCode("F001")
                .factoryName("테스트공장")
                .isActive(true)
                .build();

        return Lines.builder()
                .factory(factory)
                .factoryId(1L)
                .lineCode("L01")
                .lineName("1라인")
                .isActive(true)
                .build();
    }

    private ItemsLines itemLine(Lines line, Items item) {
        return ItemsLines.builder()
                .line(line)
                .lineId(1L)
                .item(item)
                .itemId(1L)
                .build();
    }

    // -------------------------------------------------------------
    // 1. 상세조회 성공 테스트
    // -------------------------------------------------------------
    @Test
    @DisplayName("생산실적 상세 조회 성공")
    void getProductionPerformanceDetail_success() {

        // given
        Long perfId = 1L;

        Lines line = line();
        Items item = item();
        ItemsLines itemLine = itemLine(line, item);

        Users salesManager = user("2000001");
        Users productionManager = user("2000002");

        ProductionPlans plan = ProductionPlans.builder()
                .id(10L)
                .salesManager(salesManager)
                .salesManagerId(1L)
                .productionManager(productionManager)
                .productionManagerId(2L)
                .itemLine(itemLine)
                .itemLineId(1L)
                .documentNo("2099/01/01-1")
                .dueDate(LocalDate.now().plusDays(1))
                .build();

        ProductionPerformances perf = ProductionPerformances.builder()
                .id(perfId)
                .productionPlan(plan)
                .productionPlanId(plan.getId())
                .performanceDocumentNo("2099/01/01-1")
                .totalQty(new BigDecimal("100"))
                .performanceQty(new BigDecimal("95"))
                .performanceDefectiveRate(new BigDecimal("5"))
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now())
                .remark("테스트 실적")
                .build();

        Lots lot = Lots.builder()
                .lotNo("LOT-001")
                .itemId(1L)
                .productionPlanId(plan.getId())
                .build();

        when(performanceRepository.findById(perfId))
                .thenReturn(Optional.of(perf));

        when(lotService.getByProductionPlanId(plan.getId()))
                .thenReturn(lot);

        // when
        GetProductionPerformanceDetailResponseDto response =
                productionPerformanceService.getProductionPerformanceDetail(perfId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getDocumentNo()).isEqualTo("2099/01/01-1");
        assertThat(response.getFactoryCode()).isEqualTo("F001");
        assertThat(response.getLineCode()).isEqualTo("L01");
        assertThat(response.getSalesManagerNo()).isEqualTo("2000001");
        assertThat(response.getProductionManagerNo()).isEqualTo("2000002");
        assertThat(response.getItemCode()).isEqualTo("ITEM001");
        assertThat(response.getLotNo()).isEqualTo("LOT-001");
        assertThat(response.getTotalQty()).isEqualByComparingTo("100");
        assertThat(response.getPerformanceQty()).isEqualByComparingTo("95");

        verify(performanceRepository, times(1)).findById(perfId);
        verify(lotService, times(1)).getByProductionPlanId(plan.getId());
    }

    // -------------------------------------------------------------
    // 상세조회 실패 테스트 — 실적 Id가 없을 때
    // -------------------------------------------------------------
    @Test
    @DisplayName("생산실적 ID가 없으면 예외 발생")
    void getProductionPerformanceDetail_notFound() {

        // given
        Long perfId = 999L;

        when(performanceRepository.findById(perfId))
                .thenReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> productionPerformanceService.getProductionPerformanceDetail(perfId))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ProductionPerformanceErrorCode.PRODUCTION_PERFORMANCE_NOT_FOUND.getMessage());

        verify(performanceRepository, times(1)).findById(perfId);
    }
}
