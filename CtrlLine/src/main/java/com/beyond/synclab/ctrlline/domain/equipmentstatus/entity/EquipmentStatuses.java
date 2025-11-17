package com.beyond.synclab.ctrlline.domain.equipmentstatus.entity;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipmentstatus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
// @EntityListeners(EntityActionLogger.class) 작성해야, 로그 테이블에 자동 등록됨.
@EntityListeners(EntityActionLogger.class)
@EqualsAndHashCode(of = "id")

public class EquipmentStatuses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_status_id", updatable = false)
    private Long id; // 설비 PK

}
