package com.beyond.synclab.ctrlline.domain.log.event;

import com.beyond.synclab.ctrlline.domain.log.entity.Logs;


public record LogEvent(
    String entityName,
    Long entityId,
    Logs.ActionType actionType
) {}
