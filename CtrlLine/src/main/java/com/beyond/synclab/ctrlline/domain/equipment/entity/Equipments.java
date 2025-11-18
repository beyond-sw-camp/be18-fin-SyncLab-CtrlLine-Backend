package com.beyond.synclab.ctrlline.domain.equipment.entity;

import com.beyond.synclab.ctrlline.domain.equipmentstatus.entity.EquipmentStatuses;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.log.util.EntityActionLogger;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
// @EntityListeners(EntityActionLogger.class) 작성해야, 로그 테이블에 자동 등록됨.
@EntityListeners(EntityActionLogger.class)
@EqualsAndHashCode(of = "id")
public class Equipments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id", updatable = false)
    private Long id; // 설비 PK

    // ───────── FK 영역 ─────────
    @JoinColumn(name = "line_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @ManyToOne(fetch = FetchType.LAZY)
    private Lines line; // 라인 FK

    @JoinColumn(name = "equipment_status_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @ManyToOne(fetch = FetchType.LAZY)
    private EquipmentStatuses equipmentStatus; // 설비상태 FK

    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user; // 사용자 FK

    // ───────── 기본 정보 ─────────
    @Column(name = "equipment_code", nullable = false, length = 32)
    private String equipmentCode; // 설비코드

    @Column(name = "equipment_name", nullable = false, length = 32)
    private String equipmentName; // 설비명

    @Column(name = "equipment_type", nullable = false, length = 32)
    private String equipmentType; // 설비유형

    // ───────── 운영 정보 ─────────
    @Column(name = "operating_time", nullable = false)
    private LocalDateTime operatingTime; // 가동시간

    @Column(name = "maintenance_history", nullable = true)
    private LocalDateTime maintenanceHistory; // 유지보수이력 (최근 날짜)

    // ───────── 생산 관련 수치 ─────────
    @Column(name = "equipment_ppm", nullable = false, precision = 10, scale = 2)
    private BigDecimal equipmentPpm; // 설비 PPM

    @Column(name = "total_count", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCount; // 투입수량

    @Column(name = "defective_count", nullable = false, precision = 10, scale = 2)
    private BigDecimal defectiveCount; // 불량수량

    // ───────── 관리 정보 ─────────
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성시각

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt; // 수정시각

    @Column(name = "is_active", nullable = false)
    private Boolean isActive; // 사용여부

    // 사용 여부 업데이트
    public void updateStatus(boolean isActive) {
        this.isActive = isActive;
    }

    // 담당자 업데이트
    public void updateManager(Users manager){
        this.user = manager;
    }
}
