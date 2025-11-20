package com.beyond.synclab.ctrlline.domain.productionplan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefective;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectiveXref;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveXrefRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.DefectiveTelemetryPayload;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanDefectiveXrefServiceTest {

    @Mock
    private ProductionPlanRepository productionPlanRepository;
    @Mock
    private PlanDefectiveRepository planDefectiveRepository;
    @Mock
    private PlanDefectiveXrefRepository planDefectiveXrefRepository;

    private PlanDefectiveXrefService planDefectiveXrefService;

    @BeforeEach
    void setUp() {
        planDefectiveXrefService = new PlanDefectiveXrefService(
                productionPlanRepository,
                planDefectiveRepository,
                planDefectiveXrefRepository
        );
    }

    @Test
    void linkPlanDefective_updatesWithLatestQuantityWhenReportedIncreases() throws Exception {
        Long defectiveId = 10L;
        String orderNo = "PP-001";
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .orderNo(orderNo)
                .defectiveQuantity(BigDecimal.valueOf(7))
                .build();
        ProductionPlans plan = mock(ProductionPlans.class);
        when(plan.getId()).thenReturn(5L);
        PlanDefective planDefective = PlanDefective.builder()
                .id(20L)
                .productionPlanId(plan.getId())
                .defectiveDocumentNo("DOC-1")
                .build();
        PlanDefectiveXref existing = PlanDefectiveXref.builder()
                .id(30L)
                .planDefectiveId(planDefective.getId())
                .defectiveId(defectiveId)
                .defectiveQty(BigDecimal.valueOf(15))
                .build();
        setLastReportedCache(planDefective.getId(), defectiveId, BigDecimal.valueOf(5));
        when(productionPlanRepository.findByDocumentNo(orderNo)).thenReturn(Optional.of(plan));
        when(planDefectiveRepository.findByProductionPlanId(plan.getId())).thenReturn(Optional.of(planDefective));
        when(planDefectiveXrefRepository.findByPlanDefectiveIdAndDefectiveId(planDefective.getId(), defectiveId))
                .thenReturn(Optional.of(existing));

        planDefectiveXrefService.linkPlanDefective(defectiveId, payload);

        ArgumentCaptor<PlanDefectiveXref> captor = ArgumentCaptor.forClass(PlanDefectiveXref.class);
        verify(planDefectiveXrefRepository, times(1)).save(captor.capture());
        // base=10 (15-5) + new(7) = 17
        assertThat(captor.getValue().getDefectiveQty()).isEqualByComparingTo(BigDecimal.valueOf(17));
    }

    @Test
    void linkPlanDefective_addsToTotalWhenReportedValueResets() throws Exception {
        Long defectiveId = 11L;
        String orderNo = "PP-002";
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .orderNo(orderNo)
                .defectiveQuantity(BigDecimal.valueOf(3))
                .build();
        ProductionPlans plan = mock(ProductionPlans.class);
        when(plan.getId()).thenReturn(6L);
        PlanDefective planDefective = PlanDefective.builder()
                .id(21L)
                .productionPlanId(plan.getId())
                .defectiveDocumentNo("DOC-2")
                .build();
        PlanDefectiveXref existing = PlanDefectiveXref.builder()
                .id(31L)
                .planDefectiveId(planDefective.getId())
                .defectiveId(defectiveId)
                .defectiveQty(BigDecimal.valueOf(12))
                .build();
        setLastReportedCache(planDefective.getId(), defectiveId, BigDecimal.valueOf(12));
        when(productionPlanRepository.findByDocumentNo(orderNo)).thenReturn(Optional.of(plan));
        when(planDefectiveRepository.findByProductionPlanId(plan.getId())).thenReturn(Optional.of(planDefective));
        when(planDefectiveXrefRepository.findByPlanDefectiveIdAndDefectiveId(planDefective.getId(), defectiveId))
                .thenReturn(Optional.of(existing));

        planDefectiveXrefService.linkPlanDefective(defectiveId, payload);

        ArgumentCaptor<PlanDefectiveXref> captor = ArgumentCaptor.forClass(PlanDefectiveXref.class);
        verify(planDefectiveXrefRepository).save(captor.capture());
        assertThat(captor.getValue().getDefectiveQty()).isEqualByComparingTo(BigDecimal.valueOf(15));
    }

    @Test
    void linkPlanDefective_createsNewRecordWithInitialValues() {
        Long defectiveId = 12L;
        String orderNo = "PP-003";
        BigDecimal payloadQty = BigDecimal.valueOf(4);
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .orderNo(orderNo)
                .defectiveQuantity(payloadQty)
                .build();
        ProductionPlans plan = mock(ProductionPlans.class);
        when(plan.getId()).thenReturn(7L);
        PlanDefective planDefective = PlanDefective.builder()
                .id(22L)
                .productionPlanId(plan.getId())
                .defectiveDocumentNo("DOC-3")
                .build();
        when(productionPlanRepository.findByDocumentNo(orderNo)).thenReturn(Optional.of(plan));
        when(planDefectiveRepository.findByProductionPlanId(plan.getId())).thenReturn(Optional.of(planDefective));
        when(planDefectiveXrefRepository.findByPlanDefectiveIdAndDefectiveId(planDefective.getId(), defectiveId))
                .thenReturn(Optional.empty());

        planDefectiveXrefService.linkPlanDefective(defectiveId, payload);

        ArgumentCaptor<PlanDefectiveXref> captor = ArgumentCaptor.forClass(PlanDefectiveXref.class);
        verify(planDefectiveXrefRepository).save(captor.capture());
        assertThat(captor.getValue().getDefectiveQty()).isEqualByComparingTo(payloadQty);
    }

    @SuppressWarnings("unchecked")
    private void setLastReportedCache(Long planDefectiveId, Long defectiveId, BigDecimal qty) throws Exception {
        Field field = PlanDefectiveXrefService.class.getDeclaredField("lastReportedByPlanAndDefect");
        field.setAccessible(true);
        Map<String, BigDecimal> cache = (Map<String, BigDecimal>) field.get(planDefectiveXrefService);
        cache.put(planDefectiveId + ":" + defectiveId, qty);
    }
}
