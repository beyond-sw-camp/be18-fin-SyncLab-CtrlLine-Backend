package com.beyond.synclab.ctrlline.domain.equipment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
public class Equipments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id", updatable = false)
    private Long equipmentId; // 설비 PK

    // ───────── FK 영역 ─────────
    @Column(name = "line_id", nullable = false)
    private Long lineId; // 라인 FK

    @Column(name = "equipment_status_id", nullable = false)
    private Long equipmentStatusId; // 설비상태 FK

    @Column(name = "user_id", nullable = false)
    private Long userId; // 사용자 FK

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

    @Column(name = "maintenance_history", nullable = false)
    private LocalDateTime maintenanceHistory; // 유지보수이력 (최근 날짜)

    // ───────── 생산 관련 수치 ─────────
    @Column(name = "equipment_ppm", nullable = false, precision = 10, scale = 2)
    private BigDecimal equipmentPpm; // 설비 PPM

    @Column(name = "total_count", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCount; // 투입수량

    @Column(name = "defective_count", nullable = false, precision = 10, scale = 2)
    private BigDecimal defectiveCount; // 불량수량

    // ───────── 관리 정보 ─────────
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성시간

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정시간

    @Column(name = "is_active", nullable = false)
    private Boolean isActive; // 사용여부
}
