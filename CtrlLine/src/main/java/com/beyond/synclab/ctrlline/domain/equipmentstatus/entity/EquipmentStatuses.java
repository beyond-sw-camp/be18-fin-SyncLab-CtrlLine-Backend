package com.beyond.synclab.ctrlline.domain.equipmentstatus.entity;

import com.beyond.synclab.ctrlline.domain.log.util.EntityActionLogger;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(EntityActionLogger.class)
@EqualsAndHashCode(of = "id")

public class EquipmentStatuses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_status_id", updatable = false)
    // 설비상태 PK
    private Long id;

    @Column(name = "equipment_status_code", nullable = false, length = 32)
    // 설비상태코드
    private String equipmentStatusCode;

    @Column(name = "equipment_status_name", nullable = false, length = 32)
    // 설비상태명
    private String equipmentStatusName;

    @Column(name = "description", length = 256)
    // 설명
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    // 생성시각
    private LocalDateTime createdAt;
}
