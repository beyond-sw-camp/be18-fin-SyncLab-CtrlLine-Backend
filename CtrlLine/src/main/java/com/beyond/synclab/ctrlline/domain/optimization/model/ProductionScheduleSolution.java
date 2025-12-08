package com.beyond.synclab.ctrlline.domain.optimization.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@PlanningSolution
public class ProductionScheduleSolution {

    /** 최적화 중인 라인 식별자 */
    private String lineCode;

    /** 최적화 대상 작업 목록 */
    @PlanningEntityCollectionProperty
    private List<ProductionPlanAssignment> assignments;

    /** 가능한 StartTime 값 범위 */
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "startTimeRange")
    private List<LocalDateTime> startTimeRange;

    /** 최적화 결과 점수 */
    @PlanningScore
    private HardSoftScore score;
}
