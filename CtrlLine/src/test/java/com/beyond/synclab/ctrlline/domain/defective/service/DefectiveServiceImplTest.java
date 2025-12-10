package com.beyond.synclab.ctrlline.domain.defective.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.errorcode.DefectiveErrorCode;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectiveXrefs;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectives;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveXrefRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.entity.Defectives;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("불량서비스 테스트")
class DefectiveServiceImplTest {
    @Mock
    private PlanDefectiveRepository planDefectiveRepository;

    @Mock
    private PlanDefectiveXrefRepository planDefectiveXrefRepository;

    @Mock
    private ProductionPerformanceRepository productionPerformanceRepository;

    @Mock
    private LotRepository lotRepository;

    @InjectMocks
    private DefectiveServiceImpl defectiveService;

    private Factories factory;
    private Lines line;
    private Items item;
    private ItemsLines itemsLine;
    private ProductionPlans plan;
    private PlanDefectives planDefectives;
    private Equipments equipment;
    private Defectives defective;
    private PlanDefectiveXrefs planDefectiveXref;

    @BeforeEach
    void setUp() {
        factory = Factories.builder()
            .id(1L)
            .factoryCode("F001")
            .factoryName("A공장")
            .build();

        line = Lines.builder()
            .id(1L)
            .lineName("1호라인")
            .factory(factory)
            .build();

        item = Items.builder()
            .itemCode("I001")
            .itemName("테스트품목")
            .itemSpecification("SPEC")
            .itemUnit("EA")
            .build();

        itemsLine = ItemsLines.builder()
            .item(item)
            .line(line)
            .build();

        plan = ProductionPlans.builder()
            .plannedQty(BigDecimal.valueOf(100))
            .itemLine(itemsLine)
            .build();

        planDefectives = PlanDefectives.builder()
            .id(1L)
            .defectiveDocumentNo("2099/01/01-1")
            .productionPlan(plan)
            .build();

        equipment = Equipments.builder()
            .equipmentName("설비1")
            .equipmentCode("EQP-1001")
            .build();

        defective = Defectives.builder()
            .defectiveCode("D001")
            .defectiveName("스크래치")
            .defectiveType("TYPE1")
            .equipment(equipment)
            .build();

        planDefectiveXref = PlanDefectiveXrefs.builder()
            .defective(defective)
            .defectiveQty(BigDecimal.valueOf(5))
            .build();
    }

    @Test
    @DisplayName("불량 상세 조회 - 정상 흐름")
    void getDefective_success() {
        // given
        Long id = 1234L;
        PlanDefectiveXrefs pdxA = planDefectiveXref.toBuilder()
            .id(1L)
            .defectiveQty(BigDecimal.valueOf(100))
            .build();

        PlanDefectiveXrefs pdxB = planDefectiveXref.toBuilder()
            .id(2L)
            .defectiveQty(BigDecimal.valueOf(100))
            .build();

        when(planDefectiveRepository.findById(id)).thenReturn(Optional.of(planDefectives));
        when(planDefectiveXrefRepository.findAllByPlanDefectiveId(1L)).thenReturn(List.of(pdxA, pdxB));

        // when
        GetDefectiveDetailResponseDto result = defectiveService.getDefective(id);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFactoryName()).isEqualTo("A공장");
        assertThat(result.getLineName()).isEqualTo("1호라인");
        assertThat(result.getItemCode()).isEqualTo("I001");
        assertThat(result.getDefectiveItems()).hasSize(2);
        assertThat(result.getDefectiveItems().getFirst().getDefectiveCode()).isEqualTo("D001");
        assertThat(result.getDefectiveItems().getFirst().getDefectiveRate()).isEqualTo(0.5); // 100 / 200

        verify(planDefectiveRepository, times(1)).findById(id);
        verify(planDefectiveXrefRepository, times(1)).findAllByPlanDefectiveId(1L);
    }

    @Test
    @DisplayName("불량 상세 조회 - 대상 없음 예외 발생")
    void getDefective_notFound() {
        // given
        Long id = 1234L;

        when(planDefectiveRepository.findById(id))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> defectiveService.getDefective(id))
            .isInstanceOf(AppException.class)
            .hasMessageContaining(DefectiveErrorCode.PLAN_DEFECTIVE_NOT_FOUND.getMessage());

        verify(planDefectiveRepository, times(1)).findById(id);
        verify(planDefectiveXrefRepository, never()).findAllByPlanDefectiveId(any());
    }
}