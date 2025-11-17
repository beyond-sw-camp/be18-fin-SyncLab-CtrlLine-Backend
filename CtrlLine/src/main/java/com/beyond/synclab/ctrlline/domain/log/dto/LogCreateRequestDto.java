package com.beyond.synclab.ctrlline.domain.log.dto;

import com.beyond.synclab.ctrlline.domain.log.entity.Logs;
import com.beyond.synclab.ctrlline.domain.log.entity.Logs.ActionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogCreateRequestDto {
    private Long userId;
    private ActionType actionType;
    private String entityName;
    private Long entityId;

    public Logs toEntity() {
        return Logs.builder()
            .entityName(entityName)
            .actionType(actionType)
            .entityId(entityId)
            .userId(userId)
            .build();
    }
}
