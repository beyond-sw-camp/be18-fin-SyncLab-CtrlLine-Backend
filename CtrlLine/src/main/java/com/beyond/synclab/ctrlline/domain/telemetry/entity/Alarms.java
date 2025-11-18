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
@Table(name = "alarm")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Alarms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipments equipment;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "alarm_code", nullable = false, length = 32)
    private String alarmCode;

    @Column(name = "alarm_name", nullable = false, length = 32)
    private String alarmName;

    @Column(name = "alarm_type", nullable = false, length = 32)
    private String alarmType;

    @Column(name = "alarm_level", length = 32)
    private String alarmLevel;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(name = "cleared_at")
    private LocalDateTime clearedAt;

    @Column(name = "alarm_cause")
    private String alarmCause;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
