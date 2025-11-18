package com.beyond.synclab.ctrlline.domain.log.dto;

import com.beyond.synclab.ctrlline.domain.log.entity.Logs;
import com.beyond.synclab.ctrlline.domain.log.entity.Logs.ActionType;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LogListResponseDto {
    private final Long logId;
    private final String entityName;
    private final Long entityId;
    private final Long userId;
    private final ActionType actionType;
    private final LocalDateTime createdAt;

    public static LogListResponseDto fromEntity(Logs log) {
        return LogListResponseDto.builder()
            .logId(log.getId())
            .entityName(log.getEntityName())
            .entityId(log.getEntityId())
            .userId(log.getUserId())
            .actionType(log.getActionType())
            .createdAt(log.getCreatedAt())
            .build();
    }
}
