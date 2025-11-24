package com.beyond.synclab.ctrlline.domain.productionplan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefective;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectiveXref;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveXrefRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.DefectiveTelemetryPayload;
import java.math.BigDecimal;
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
    @Mock
    private PlanDefectiveLastReportedCache lastReportedCache;

    private PlanDefectiveXrefService planDefectiveXrefService;

    @BeforeEach
    void setUp() {
        planDefectiveXrefService = new PlanDefectiveXrefService(
                productionPlanRepository,
                planDefectiveRepository,
                planDefectiveXrefRepository,
                lastReportedCache
        );
    }

    @Test
    void linkPlanDefective_updatesWithLatestQuantityWhenReportedIncreases() throws Exception {
        Long defectiveId = 10L;
        String orderNo = "PP-001";
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .orderNo(orderNo)
                .defectiveQuantity(BigDecimal.valueOf(7))
                .equipmentId(100L)
                .defectiveType("1")
                .build();
        ProductionPlans plan = mock(ProductionPlans.class);
        when(plan.getId()).thenReturn(5L);
        when(plan.getStatus()).thenReturn(ProductionPlans.PlanStatus.RUNNING);
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
        when(lastReportedCache.get(planDefective.getId(), defectiveId, "id:100"))
                .thenReturn(Optional.of(BigDecimal.valueOf(5)));
        when(productionPlanRepository.findByDocumentNo(orderNo)).thenReturn(Optional.of(plan));
        when(planDefectiveRepository.findByProductionPlanId(plan.getId())).thenReturn(Optional.of(planDefective));
        when(planDefectiveXrefRepository.findByPlanDefectiveIdAndDefectiveId(planDefective.getId(), defectiveId))
                .thenReturn(Optional.of(existing));

        planDefectiveXrefService.linkPlanDefective(defectiveId, payload);

        ArgumentCaptor<PlanDefectiveXref> captor = ArgumentCaptor.forClass(PlanDefectiveXref.class);
        verify(planDefectiveXrefRepository, times(1)).save(captor.capture());
        // base=10 (15-5) + new(7) = 17
        assertThat(captor.getValue().getDefectiveQty()).isEqualByComparingTo(BigDecimal.valueOf(17));
        verify(lastReportedCache).save(planDefective.getId(), defectiveId, "id:100", BigDecimal.valueOf(7));
    }

    @Test
    void linkPlanDefective_addsToTotalWhenReportedValueResets() throws Exception {
        Long defectiveId = 11L;
        String orderNo = "PP-002";
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .orderNo(orderNo)
                .defectiveQuantity(BigDecimal.valueOf(3))
                .equipmentCode("EQP-2")
                .defectiveType("2")
                .build();
        ProductionPlans plan = mock(ProductionPlans.class);
        when(plan.getId()).thenReturn(6L);
        when(plan.getStatus()).thenReturn(ProductionPlans.PlanStatus.RUNNING);
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
        when(lastReportedCache.get(planDefective.getId(), defectiveId, "code:EQP-2"))
                .thenReturn(Optional.of(BigDecimal.valueOf(12)));
        when(productionPlanRepository.findByDocumentNo(orderNo)).thenReturn(Optional.of(plan));
        when(planDefectiveRepository.findByProductionPlanId(plan.getId())).thenReturn(Optional.of(planDefective));
        when(planDefectiveXrefRepository.findByPlanDefectiveIdAndDefectiveId(planDefective.getId(), defectiveId))
                .thenReturn(Optional.of(existing));

        planDefectiveXrefService.linkPlanDefective(defectiveId, payload);

        ArgumentCaptor<PlanDefectiveXref> captor = ArgumentCaptor.forClass(PlanDefectiveXref.class);
        verify(planDefectiveXrefRepository).save(captor.capture());
        assertThat(captor.getValue().getDefectiveQty()).isEqualByComparingTo(BigDecimal.valueOf(15));
        verify(lastReportedCache).save(planDefective.getId(), defectiveId, "code:EQP-2", BigDecimal.valueOf(3));
    }

    @Test
    void linkPlanDefective_createsNewRecordWithInitialValues() {
        Long defectiveId = 12L;
        String orderNo = "PP-003";
        BigDecimal payloadQty = BigDecimal.valueOf(4);
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .orderNo(orderNo)
                .defectiveQuantity(payloadQty)
                .defectiveType("3")
                .build();
        ProductionPlans plan = mock(ProductionPlans.class);
        when(plan.getId()).thenReturn(7L);
        when(plan.getStatus()).thenReturn(ProductionPlans.PlanStatus.RUNNING);
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
        verify(lastReportedCache).save(planDefective.getId(), defectiveId, "default", payloadQty);
    }

    @Test
    void linkPlanDefective_skipsWhenPlanNotRunning() {
        Long defectiveId = 20L;
        String orderNo = "PP-004";
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .orderNo(orderNo)
                .defectiveQuantity(BigDecimal.TEN)
                .defectiveType("1")
                .build();
        ProductionPlans plan = mock(ProductionPlans.class);
        when(plan.getStatus()).thenReturn(ProductionPlans.PlanStatus.PENDING);
        when(productionPlanRepository.findByDocumentNo(orderNo)).thenReturn(Optional.of(plan));

        planDefectiveXrefService.linkPlanDefective(defectiveId, payload);

        verify(planDefectiveRepository, never()).findByProductionPlanId(any());
        verify(planDefectiveXrefRepository, never()).save(any());
    }
}
