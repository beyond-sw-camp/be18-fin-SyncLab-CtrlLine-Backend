package com.beyond.synclab.ctrlline.domain.item.entity;

import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.*;
import org.flywaydb.core.internal.util.StringUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "item",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_item_code",
                        columnNames = "item_code"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(of = "id")
public class Items {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", updatable = false)
    private Long id;

    @Column(name = "item_code", nullable = false, unique = true, length = 32)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 32)
    private String itemName;

    @Column(name = "item_specification", length = 32)
    private String itemSpecification;

    @Column(name = "item_unit", nullable = false, length = 32)
    private String itemUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false, length = 32)
    private ItemStatus itemStatus; // 원재료 / 부재료 / 반제품 / 완제품

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================
    // 도메인 메서드
    // ======================

    // 품목 수정
    public void updateItem(UpdateItemRequestDto dto) {
        if (StringUtils.hasText(dto.getItemCode())) this.itemCode = dto.getItemCode().trim();
        if (StringUtils.hasText(dto.getItemName())) this.itemName = dto.getItemName().trim();
        if (StringUtils.hasText(dto.getItemSpecification())) this.itemSpecification = dto.getItemSpecification().trim();
        if (StringUtils.hasText(dto.getItemUnit())) this.itemUnit = dto.getItemUnit().trim();
        if (dto.getItemStatus() != null) this.itemStatus = dto.getItemStatus();
        if (dto.getIsActive() != null) this.isActive = dto.getIsActive();
        this.updatedAt = LocalDateTime.now();
    }

    // 품목 사용여부 변경 (다건 수정 대응)
    public void updateItemAct(Boolean isActive) {
        if (isActive != null) {
            this.isActive = isActive;
            this.updatedAt = LocalDateTime.now();
        }
    }
}
