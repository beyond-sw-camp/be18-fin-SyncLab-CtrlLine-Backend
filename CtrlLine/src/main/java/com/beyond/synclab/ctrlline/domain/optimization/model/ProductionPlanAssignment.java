package com.beyond.synclab.ctrlline.domain.optimization.model;

import com.beyond.synclab.ctrlline.domain.optimization.listener.EndTimeUpdatingListener;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.ShadowVariable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@PlanningEntity
public class ProductionPlanAssignment {

    /** 원본 엔티티 ID */
    @PlanningId
    private Long planId;

    /** Document No */
    private String documentNo;

    private PlanStatus planStatus;
    private BigDecimal plannedQty;
    private Long itemId;
    private String itemName;
    private String itemCode;

    /** 작업의 예상 소요시간(분 단위). ShadowVariable 로 end 계산에 사용 */
    private long durationMinutes;

    /** 납기일(정오 기준) */
    private LocalDateTime dueDateTime;

    /** Confirmed 여부 (MANAGER 모드에서 lock 처리) */
    private boolean confirmed;

    /** 역할 기반 잠금 여부 (Confirmed & MANAGER 인 경우) */
    private boolean locked;

    /** 기존 DB 값 - Manager 모드에서 Confirmed 잠금 처리용 */
    private LocalDateTime originalStartTime;

    /** Optimizer 가 조정할 수 있도록 PlanningVariable 지정 */
    @PlanningVariable(valueRangeProviderRefs = {"startTimeRange"})
    private LocalDateTime startTime;

    /**
     * Shadow variable – startTime 변경 시 Listener가 업데이트
     * sourceVariableName = "startTime" 꼭 필요
     */
    @ShadowVariable(
        variableListenerClass = EndTimeUpdatingListener.class,
        sourceVariableName = "startTime"
    )
    private LocalDateTime endTime;

}