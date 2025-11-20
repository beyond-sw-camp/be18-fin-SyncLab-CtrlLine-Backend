package com.beyond.synclab.ctrlline.domain.process.entity;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.log.util.EntityActionLogger;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "process_id", updatable = false)
    // 공정 PK
    private Long id;

    // <========FK=========>
    @Column(name = "equipment_id")
    private Long equipmentId; // 설비 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", insertable = false, updatable = false)
    private Equipments equipment;

    @Column(name = "user_id")
    private Long userId; // 사용자 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private Users user;

    // <==========일반 컬럼들=========>
    @Column(name = "process_code", nullable = false, length = 32)
    private String processCode; // 공정코드

    @Column(name = "process_name", nullable = false, length = 32)
    private String processName; // 공정명

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 생성시각

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정시각

    @Column(name = "is_active", nullable = false)
    private boolean isActive; // 사용여부

    // 사용 여부 업데이트
    public void updateStatus(boolean isActive) {
        this.isActive = isActive;
    }

    // 담당자 업데이트 (무조건 Long userId로 받아야, DB도 같이 변경됩니다!)
    public void updateManager(Users newManager) {
        this.user = newManager;
        this.userId = newManager.getId();
    }
}
