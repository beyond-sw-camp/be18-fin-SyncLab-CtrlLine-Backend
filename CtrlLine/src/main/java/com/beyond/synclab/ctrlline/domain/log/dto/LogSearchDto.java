package com.beyond.synclab.ctrlline.domain.log.dto;

import com.beyond.synclab.ctrlline.domain.log.entity.Logs.ActionType;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record LogSearchDto(
    Long userId,
    String entityName,
    ActionType actionType,
    LocalDate fromDate,
    LocalDate toDate
) {
}
