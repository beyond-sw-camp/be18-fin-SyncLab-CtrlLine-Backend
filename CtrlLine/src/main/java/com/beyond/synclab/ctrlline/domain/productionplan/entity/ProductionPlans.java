package com.beyond.synclab.ctrlline.domain.productionplan.entity;

import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.log.util.EntityActionLogger;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Builder(toBuilder = true)
@Entity
@Table(
        name = "production_plan",
        indexes = {
                @Index(
                        name = "idx_production_plan_item_line_id",
                        columnList = "item_line_id"
                ),
                @Index(
                        name = "idx_production_plan_sales_manager_id",
                        columnList = "sales_manager_id"
                ),
                @Index(
                        name = "idx_production_plan_production_manager_id",
                        columnList = "production_manager_id"
                ),
                @Index(
                        name = "idx_production_plan_due_date",
                        columnList = "due_date"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(EntityActionLogger.class)
@EqualsAndHashCode(of = "id")
public class ProductionPlans {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "production_plan_id")
    private Long id;

    @Column(name = "item_line_id")
    private Long itemLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_line_id", insertable = false, updatable = false)
    private ItemsLines itemLine;

    @Column(name = "sales_manager_id")
    private Long salesManagerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_manager_id", insertable = false, updatable = false)
    private Users salesManager;

    @Column(name = "production_manager_id")
    private Long productionManagerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_manager_id", insertable = false, updatable = false)
    private Users productionManager;

    @Column(name = "plan_document_no", nullable = false, unique = true)
    private String documentNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "production_plan_status", nullable = false)
    @Builder.Default
    private PlanStatus status = PlanStatus.PENDING;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "planned_qty", nullable = false)
    private java.math.BigDecimal plannedQty;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "remark")
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void markDispatched() {
        this.status = PlanStatus.RUNNING;
    }

    public void markDispatchFailed() {
        this.status = PlanStatus.RETURNED;
    }

    public int commandQuantity() {
        return plannedQty != null ? plannedQty.intValue() : 0;
    }

    public void updateStartTime(LocalDateTime localDateTime) {
        this.startTime = localDateTime;
    }

    public void updateEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void updateStatus(PlanStatus requestedStatus) {
        this.status = requestedStatus;
    }

    // 기존 생산계획은 pending 이나 confirmed 일때만 변경 가능하다.
    public boolean isUpdatable() {
        return (status == PlanStatus.PENDING || status == PlanStatus.CONFIRMED);
    }

    public void updatePeriod(Duration originalPeriod) {
        this.startTime = this.startTime.plus(originalPeriod);
        this.endTime = this.endTime.plus(originalPeriod);
    }

    public void update(UpdateProductionPlanRequestDto dto, Users salesManager, Users productionManager, ItemsLines itemLine) {
        if (dto.getStatus() != null) {
            this.status = dto.getStatus();
        }

        if (salesManager != null) {
            this.salesManagerId = salesManager.getId();
            this.salesManager = salesManager;
        }

        if (productionManager != null) {
            this.productionManagerId = productionManager.getId();
            this.productionManager = productionManager;
        }

        if (dto.getStartTime() != null) {
            this.startTime = dto.getStartTime();
        }

        if (dto.getEndTime() != null) {
            this.endTime = dto.getEndTime();
        }

        if (itemLine != null) {
            this.itemLineId = itemLine.getId();
            this.itemLine = itemLine;
        }

        if (dto.getDueDate() != null) {
            this.dueDate = dto.getDueDate();
        }

        if (dto.getRemark() != null) {
            this.remark = dto.getRemark();
        }
    }

    public enum PlanStatus {
        PENDING,
        CONFIRMED,
        RUNNING,
        COMPLETED,
        RETURNED
    }
}
