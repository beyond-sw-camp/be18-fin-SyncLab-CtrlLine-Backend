package com.beyond.synclab.ctrlline.domain.productionplan.entity;

import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.log.util.EntityActionLogger;
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
@Builder
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

    public enum PlanStatus {
        PENDING,
        CONFIRMED,
        RUNNING,
        COMPLETED,
        RETURNED
    }
}
