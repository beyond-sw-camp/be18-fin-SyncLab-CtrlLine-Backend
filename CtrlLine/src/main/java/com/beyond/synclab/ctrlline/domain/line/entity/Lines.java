package com.beyond.synclab.ctrlline.domain.line.entity;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder(toBuilder = true)
@Table(
        name = "line",
        uniqueConstraints = {
        @UniqueConstraint(
                name = "uq_line_code",
                columnNames = "line_code"
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(EntityActionLogger.class)
@EqualsAndHashCode(of = "id")
public class Lines {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id", updatable = false)
    private Long id;

    @Column(name = "user_id")
    private Long userId;  // 실제 저장되는 값

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false, insertable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Users user;

    @Column(name = "factory_id")
    private Long factoryId; // 실제 저장되는 값

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", updatable = false, insertable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Factories factory; // 조회용

    // JOIN을 위해 추가된 관계 코드, 조회용으로 사용 불가
    @OneToMany(mappedBy = "line", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ItemsLines> itemLines = new ArrayList<>();

    @Column(name = "line_code", nullable = false, unique = true)
    private String lineCode;

    @Column(name = "line_name", nullable = false, length = 100)
    private String lineName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Lines(Long id, Factories factory, String lineCode) {
        this.id = id;
        this.factory = factory;
        this.lineCode = lineCode;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lineName = "";
    }

    public static Lines of(Long id, Long factoryId, String lineCode) {
        Factories factory = Factories.builder()
                                     .id(factoryId)
                                     .build();
        return new Lines(id, factory, lineCode);
    }

    public void updateActive(Boolean isActive) {
        if (isActive != null) {
            this.isActive = isActive;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void updateLineName(String lineName) {
        if (StringUtils.hasText(lineName)) {
            this.lineName = lineName.trim();
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void updateManager(Users newManager) {
        if (newManager != null) {
            this.user = newManager;
            this.userId = newManager.getId();
            this.updatedAt = LocalDateTime.now();
        }
    }

    public boolean isActivated() {
        return this.isActive;
    }
}
