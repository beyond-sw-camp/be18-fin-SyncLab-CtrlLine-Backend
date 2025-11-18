package com.beyond.synclab.ctrlline.domain.line.entity;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.log.util.EntityActionLogger;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
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
    @Column(name = "line_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factories factory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "line_code", nullable = false, unique = true)
    private String lineCode;

    @Column(name = "line_name", nullable = false, length = 100)
    private String lineName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    public Lines(Long id, Factories factory, String lineCode) {
        this.id = id;
        this.factory = factory;
        this.lineCode = lineCode;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lineName = "";
    }

    public static Lines of(Long id, Factories factory, String lineCode) {
        return new Lines(id, factory, lineCode);
    }
}
