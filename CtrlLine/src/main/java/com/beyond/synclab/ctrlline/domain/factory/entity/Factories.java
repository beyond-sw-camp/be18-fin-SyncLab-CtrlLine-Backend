package com.beyond.synclab.ctrlline.domain.factory.entity;

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
@Table(name = "factory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(EntityActionLogger.class)
@EqualsAndHashCode(of = "id")
public class Factories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "factory_id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users;

    @Column(name = "factory_code", nullable = false, unique = true)
    private String factoryCode;

    @Column(name = "factory_name", nullable = false)
    private String factoryName;

    @Column(name="is_active", nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateStatus(boolean isActive) {
        this.isActive = isActive;
    }

}
