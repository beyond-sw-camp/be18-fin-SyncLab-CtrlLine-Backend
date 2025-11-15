package com.beyond.synclab.ctrlline.domain.productionplan.entity;

import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.log.util.EntityActionLogger;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import jakarta.persistence.*;

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
                        name = "idx_production_plan_line_id",
                        columnList = "line_id"
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Lines line;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_manager_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Users salesManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_manager_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Users productionManager;

    @Column(name = "document_no", nullable = false, unique = true)
    private String documentNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "production_plan_status", nullable = false)
    private PlanStatus status;

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

    public enum PlanStatus {
        PENDING,
        CONFIRMED,
        RUNNING,
        COMPLETED,
        RETURNED
    }
}
