package com.beyond.synclab.ctrlline.domain.process.entity;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;

import com.beyond.synclab.ctrlline.domain.log.util.EntityActionLogger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "process")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(EntityActionLogger.class)
@EqualsAndHashCode(of = "id")

public class Processes {
    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "process_id", updatable = false)
    // 공정PK
    private Long id;

    // FK
    @JoinColumn(name = "equipment_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    // 설비FK
    private Equipments equipment;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    // 사용자FK
    private Users user;

    // <======== 일반 컬럼들 ==========>

    @Column(name = "process_code", nullable = false, length = 32)
    // 공정코드
    private String processCode;

    @Column(name = "process_name", nullable = false, length = 32)
    // 공정명
    private String processName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    // 생성시각
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    // 수정시각
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    // 사용여부
    private boolean isActive;







}
