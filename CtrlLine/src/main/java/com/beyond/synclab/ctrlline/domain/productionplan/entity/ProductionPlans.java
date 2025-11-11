package com.beyond.synclab.ctrlline.domain.productionplan.entity;

import com.beyond.synclab.ctrlline.domain.log.util.EntityActionLogger;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
@Table(name = "production_plan")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(EntityActionLogger.class)
@EqualsAndHashCode(of = "id")
public class ProductionPlans {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "production_plan_id")
    private Long id;

    @Column(name = "line_id", nullable = false)
    private Long lineId;

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

    @Transient
    private String lineCode;

    public void markDispatched() {
        this.status = PlanStatus.RUNNING;
    }

    public void markDispatchFailed() {
        this.status = PlanStatus.RETURNED;
    }

    public void assignLineCode(String lineCode) {
        this.lineCode = lineCode;
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
