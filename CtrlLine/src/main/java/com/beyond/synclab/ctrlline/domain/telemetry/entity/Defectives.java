package com.beyond.synclab.ctrlline.domain.telemetry.entity;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "defective")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Defectives {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "defective_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipments equipment;

    @Column(name = "document_no", nullable = false, length = 32)
    private String documentNo;

    @Column(name = "defective_code", nullable = false, length = 32)
    private String defectiveCode;

    @Column(name = "defective_name", nullable = false, length = 32)
    private String defectiveName;

    @Column(name = "defective_qty", nullable = false, precision = 10, scale = 2)
    private BigDecimal defectiveQty;

    @Column(name = "defective_status", nullable = false, length = 32)
    private String defectiveStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
