package com.beyond.synclab.ctrlline.domain.productionperformance.entity;

import com.beyond.synclab.ctrlline.domain.productionplans.entity.ProductionPlans;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "production_performance",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_performance_document_no",
                        columnNames = "performance_document_no"
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_plan_id", nullable = false)
    private ProductionPlans productionPlan;

    @Column(name = "performance_document_no", length = 32, nullable = false)
    private String performanceDocumentNo;

    @Column(name = "total_qty", precision = 10, scale = 2, nullable = false)
    private Double totalQty;

    @Column(name = "performance_qty", precision = 10, scale = 2, nullable = false)
    private Double performanceQty;

    @Column(name = "performance_defective_rate", precision = 10, scale = 2, nullable = false)
    private Double performanceDefectiveRate;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
