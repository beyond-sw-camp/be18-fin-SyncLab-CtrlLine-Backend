package com.beyond.synclab.ctrlline.domain.productionplan.entity;

import com.beyond.synclab.ctrlline.domain.telemetry.entity.Defectives;
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
import java.math.BigDecimal;
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
@Table(name = "plan_defective_xref")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class PlanDefectiveXrefs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "defective_xref_id")
    private Long id;

    @Column(name = "defective_id", nullable = false)
    private Long defectiveId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defective_Id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Defectives defective;

    @Column(name = "plan_defective_id", nullable = false)
    private Long planDefectiveId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_defective_Id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PlanDefectives planDefectives;

    @Column(name = "defective_qty", nullable = false, precision = 10, scale = 2)
    private BigDecimal defectiveQty;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateDefectiveQty(BigDecimal defectiveQty) {
        this.defectiveQty = defectiveQty;
    }

}
