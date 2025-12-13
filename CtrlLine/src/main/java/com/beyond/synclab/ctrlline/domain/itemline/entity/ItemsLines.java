package com.beyond.synclab.ctrlline.domain.itemline.entity;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Entity
@Table(name = "item_line")
public class ItemsLines {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_line_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", updatable = false, insertable = false)
    private Items item;

    @Column(name = "line_id")
    private Long lineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", updatable = false, insertable = false)
    private Lines line;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void inactive() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public boolean isActivated() {
        return this.isActive;
    }
}
