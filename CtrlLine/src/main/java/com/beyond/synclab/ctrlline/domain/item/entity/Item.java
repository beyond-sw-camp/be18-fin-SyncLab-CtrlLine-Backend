package com.beyond.synclab.ctrlline.domain.item.entity;

import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(of = "id")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", updatable = false)
    private Long id;

    @Column(name = "item_code", nullable = false, length = 32, unique = true)
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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // ====== 도메인 메서드 ======
    public void updateItem(String code, String name, String specification, String unit, ItemStatus status) {
        this.itemCode = code;
        this.itemName = name;
        this.itemSpecification = specification;
        this.itemUnit = unit;
        this.itemStatus = status;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
