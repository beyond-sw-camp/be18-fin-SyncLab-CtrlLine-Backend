package com.beyond.synclab.ctrlline.common.util;

import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanScheduleRequestDto;
import java.time.temporal.ChronoUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheKeyUtil {

    public String getProductionPlanScheduleKey(GetProductionPlanScheduleRequestDto dto) {
        String start = dto.startTime().truncatedTo(ChronoUnit.MINUTES).toString();
        String end = dto.endTime().truncatedTo(ChronoUnit.MINUTES).toString();
        return String.join(":", dto.factoryName(), dto.factoryCode(), dto.lineName(), dto.lineCode(), start, end);
    }
}
