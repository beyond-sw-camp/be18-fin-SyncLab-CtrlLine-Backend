package com.beyond.synclab.ctrlline.domain.production.entity;

import com.beyond.synclab.ctrlline.domain.log.util.EntityActionLogger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "line")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(EntityActionLogger.class)
public class Line {

    @Id
    @Column(name = "line_id")
    private Long id;

    @Column(name = "line_code", nullable = false)
    private String lineCode;

    public static Line of(Long id, String lineCode) {
        return new Line(id, lineCode);
    }
}
