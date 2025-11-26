package com.beyond.synclab.ctrlline.domain.productionplan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "plan_defective")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class PlanDefectives {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_defective_id")
    private Long id;

    @Column(name = "production_plan_id", nullable = false)
    private Long productionPlanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_plan_Id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ProductionPlans productionPlan;

    @Column(name = "defective_document_no", nullable = false, length = 32)
    private String defectiveDocumentNo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
