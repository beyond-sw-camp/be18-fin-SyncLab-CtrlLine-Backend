package com.beyond.synclab.ctrlline.domain.productionperformance.entity;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "production_performance",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_performance_document_no",
                        columnNames = "performance_document_no"
                ),
                @UniqueConstraint(
                        name = "uq_production_performance_plan",
                        columnNames = "production_plan_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(of = "id")
public class ProductionPerformances {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "production_performance_id", updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_plan_id", nullable = false, insertable = false, updatable = false)
    private ProductionPlans productionPlan;

    @Column(name = "production_plan_id", nullable = false)
    private Long productionPlanId;

    @Column(name = "performance_document_no", length = 32, nullable = false)
    private String performanceDocumentNo;

    @Column(name = "total_qty", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalQty;

    @Column(name = "performance_qty", precision = 10, scale = 2, nullable = false)
    private BigDecimal performanceQty;

    public BigDecimal getPerformanceDefectiveQty() {
        if (totalQty == null || performanceQty == null) return BigDecimal.ZERO;
        return totalQty.subtract(performanceQty);
    }

    @Column(name = "performance_defective_rate", precision = 10, scale = 2, nullable = false)
    private BigDecimal performanceDefectiveRate;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public void updatePerformance(BigDecimal totalQty, BigDecimal producedQty, BigDecimal defectiveRate,
                                  LocalDateTime startTime, LocalDateTime endTime) {
        this.totalQty = totalQty;
        this.performanceQty = producedQty;
        this.performanceDefectiveRate = defectiveRate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isDeleted = Boolean.FALSE;
    }

    public void updateRemark(String remark) {
        if (remark != null) {
            this.remark = remark;
        }
    }
}
