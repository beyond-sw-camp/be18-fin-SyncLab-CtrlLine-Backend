package com.beyond.synclab.ctrlline.domain.productionplan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class PlanDefectiveXref {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "defective_xref_id")
    private Long id;

    @Column(name = "defective_id", nullable = false)
    private Long defectiveId;

    @Column(name = "plan_defective_id", nullable = false)
    private Long planDefectiveId;

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

    public void increaseDefectiveQty(BigDecimal additionalQty) {
        if (additionalQty == null) {
            return;
        }
        BigDecimal current = this.defectiveQty != null ? this.defectiveQty : BigDecimal.ZERO;
        this.defectiveQty = current.add(additionalQty);
    }
}
