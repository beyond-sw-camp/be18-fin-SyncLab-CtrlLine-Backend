package com.beyond.synclab.ctrlline.domain.lot.service;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.lot.dto.response.GetLotDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.exception.LotNotFoundException;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.exception.ProductionPerformanceNotFoundException;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.serial.entity.ItemSerials;
import com.beyond.synclab.ctrlline.domain.serial.repository.ItemSerialRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotServiceTest {

    @Mock
    private LotRepository lotRepository;

    @Mock
    private ItemSerialRepository itemSerialRepository;

    @Mock
    private ProductionPerformanceRepository performanceRepository;

    @InjectMocks
    private LotServiceImpl lotService;

    // -------------------------------------------------------------
    // 공통 엔티티 생성 메서드
    // -------------------------------------------------------------
    private Users user(String empNo) {
        return Users.builder()
                .empNo(empNo)
                .name("테스트유저")
                .email(empNo + "@test.com")
                .password("1234")
                .phoneNumber("010-1234-5678")
                .hiredDate(LocalDate.now())
                .role(Users.UserRole.USER)
                .status(Users.UserStatus.ACTIVE)
                .department("QA")
                .position(Users.UserPosition.ASSISTANT)
                .address("서울")
                .build();
    }

    private Items item() {
        return Items.builder()
                .itemCode("ITEM001")
                .itemName("배터리 모듈")
                .itemSpecification("SPEC")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();
    }

    private Lines line() {
        Factories factory = Factories.builder()
                .factoryCode("F01")
                .factoryName("서울공장")
                .isActive(true)
                .build();

        return Lines.builder()
                .lineCode("L01")
                .lineName("1라인")
                .factory(factory)
                .factoryId(1L)
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
    // 1. 상세조회 정상 케이스
    // -------------------------------------------------------------
    @Test
    @DisplayName("LOT 상세조회 - 정상 조회 성공")
    void getLotDetail_success() {

        // GIVEN
        Long lotId = 1L;

        Lines line = line();
        Items item = item();
        ItemsLines itemLine = itemLine(line, item);

        Users prodManager = user("EMP100");

        ProductionPlans plan = ProductionPlans.builder()
                .id(20L)
                .productionManager(prodManager)
                .productionManagerId(2L)
                .itemLine(itemLine)
                .itemLineId(1L)
                .dueDate(LocalDate.now().plusDays(1))
                .build();

        ProductionPerformances perf = ProductionPerformances.builder()
                .id(99L)
                .productionPlan(plan)
                .productionPlanId(plan.getId())
                .performanceDocumentNo("PRD-001")
                .totalQty(new BigDecimal("500"))
                .performanceQty(new BigDecimal("480"))
                .performanceDefectiveRate(new BigDecimal("4"))
                .remark("정상 생산 완료")
                .build();

        Lots lot = Lots.builder()
                .id(lotId)
                .lotNo("20250101-1")
                .itemId(1L)
                .productionPlanId(plan.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 2, 15, 0))
                .build();

        org.springframework.test.util.ReflectionTestUtils
                .setField(lot, "productionPlan", plan);

        ItemSerials serial = ItemSerials.builder()
                .id(100L)
                .lotId(lotId)
                .serialFilePath("/serials/2025/001.gz")
                .build();

        // MOCK
        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));
        when(performanceRepository.findByProductionPlanIdAndIsDeletedFalse(plan.getId())).thenReturn(Optional.of(perf));
        when(itemSerialRepository.findByLotId(lotId)).thenReturn(Optional.of(serial));

        // WHEN
        GetLotDetailResponseDto result = lotService.getLotDetail(lotId);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getLotId()).isEqualTo(lotId);
        assertThat(result.getLotNo()).isEqualTo("20250101-1");
        assertThat(result.getFactoryCode()).isEqualTo("F01");
        assertThat(result.getLineCode()).isEqualTo("L01");
        assertThat(result.getProductionManagerNo()).isEqualTo("EMP100");
        assertThat(result.getProductionPerformanceDocNo()).isEqualTo("PRD-001");
        assertThat(result.getRemark()).isEqualTo("정상 생산 완료");
        assertThat(result.getItemCode()).isEqualTo("ITEM001");
        assertThat(result.getLotQty()).isEqualTo(500);
        assertThat(result.getPerformanceQty()).isEqualTo(480);
        assertThat(result.getDefectiveQty()).isEqualTo(20);
        assertThat(result.getDefectiveRate()).isEqualTo(4);
        assertThat(result.getSerialFilePath()).isEqualTo("/serials/2025/001.gz");

        verify(lotRepository, times(1)).findById(lotId);
        verify(performanceRepository, times(1)).findByProductionPlanIdAndIsDeletedFalse(plan.getId());
    }

    // -------------------------------------------------------------
    // 2. LOT 없음
    // -------------------------------------------------------------
    @Test
    @DisplayName("LOT 상세조회 - LOT이 없으면 LotNotFoundException 발생")
    void getLotDetail_lotNotFound() {

        when(lotRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lotService.getLotDetail(1L))
                .isInstanceOf(LotNotFoundException.class);

        verify(lotRepository, times(1)).findById(1L);
    }

    // -------------------------------------------------------------
    // 3. 실적 없음
    // -------------------------------------------------------------
    @Test
    @DisplayName("LOT 상세조회 - 실적이 없으면 ProductionPerformanceNotFoundException 발생")
    void getLotDetail_perfNotFound() {

        // LOT 객체
        Lots lot = Lots.builder()
                .id(1L)
                .productionPlanId(10L)
                .build();

        // ProductionPlan Mock 생성
        ProductionPlans plan = mock(ProductionPlans.class);
        when(plan.getId()).thenReturn(10L);

        // LOT 에 plan 주입 (Lazy 필드 해결)
        ReflectionTestUtils.setField(lot, "productionPlan", plan);

        // Mock 설정
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(performanceRepository.findByProductionPlanIdAndIsDeletedFalse(10L)).thenReturn(Optional.empty());

        // 검증
        assertThatThrownBy(() -> lotService.getLotDetail(1L))
                .isInstanceOf(ProductionPerformanceNotFoundException.class);

        verify(performanceRepository, times(1)).findByProductionPlanIdAndIsDeletedFalse(10L);
    }
}
