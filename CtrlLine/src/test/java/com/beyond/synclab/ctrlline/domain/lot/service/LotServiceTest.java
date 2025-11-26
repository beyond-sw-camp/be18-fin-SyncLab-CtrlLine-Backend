package com.beyond.synclab.ctrlline.domain.lot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LotServiceTest {

    @Mock
    private LotRepository lotRepository;

    private Clock fixedClock;

    private LotService lotService;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2025-10-30T02:45:12Z"), ZoneOffset.UTC);
        lotService = new LotService(lotRepository, fixedClock);
    }

    @Test
    void createLot_generatesLotNoLikeDefectiveRule() {
        ProductionPlans plan = samplePlan(20L);
        when(lotRepository.existsByProductionPlanId(20L)).thenReturn(false);
        when(lotRepository.findTopByLotNoStartingWithOrderByIdDesc("2025/10/30-"))
                .thenReturn(Optional.of(Lots.builder().id(1L).productionPlanId(17L).itemId(1L).lotNo("2025/10/30-2").build()));
        when(lotRepository.save(any(Lots.class))).thenAnswer(inv -> inv.getArgument(0, Lots.class));

        lotService.createLot(plan);

        ArgumentCaptor<Lots> captor = ArgumentCaptor.forClass(Lots.class);
        verify(lotRepository).save(captor.capture());
        Lots saved = captor.getValue();
        assertThat(saved.getProductionPlanId()).isEqualTo(20L);
        assertThat(saved.getItemId()).isEqualTo(100L);
        assertThat(saved.getLotNo()).isEqualTo("2025/10/30-3");
    }

    @Test
    void createLot_skipsWhenItemMissing() {
        ProductionPlans plan = samplePlan(30L).toBuilder()
                .itemLine(null)
                .build();
        lotService.createLot(plan);
        verify(lotRepository, never()).save(any());
    }

    @Test
    void createLot_skipsWhenPlanExists() {
        ProductionPlans plan = samplePlan(50L);
        when(lotRepository.existsByProductionPlanId(50L)).thenReturn(true);

        lotService.createLot(plan);

        verify(lotRepository, never()).save(any());
    }

    private ProductionPlans samplePlan(Long id) {
        LocalDate today = LocalDate.parse("2025-10-30");
        LocalDateTime now = LocalDateTime.of(2025, 10, 30, 12, 0);
        Items item = Items.builder()
                .id(100L)
                .itemCode("ITEM-001")
                .itemName("Sample")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();
        ItemsLines itemLine = ItemsLines.builder()
                .id(999L)
                .itemId(100L)
                .item(item)
                .build();
        return ProductionPlans.builder()
                .id(id)
                .documentNo("PLAN-" + id)
                .status(PlanStatus.CONFIRMED)
                .dueDate(today)
                .plannedQty(BigDecimal.TEN)
                .startTime(now)
                .endTime(now.plusHours(8))
                .itemLine(itemLine)
                .build();
    }
}
