package com.beyond.synclab.ctrlline.domain.telemetry.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = FactoryEnvironmentResponse.FactoryEnvironmentResponseBuilder.class)
@Schema(name = "FactoryEnvironmentResponse", description = "공장 온습도 데이터")
public class FactoryEnvironmentResponse {

    @Schema(description = "공장 코드")
    String factoryCode;

    @Schema(description = "공장 ID")
    Long factoryId;

    @Schema(description = "섭씨 온도")
    BigDecimal temperature;

    @Schema(description = "상대 습도")
    BigDecimal humidity;

    @Schema(description = "측정 시각")
    LocalDateTime recordedAt;

    @JsonPOJOBuilder(withPrefix = "")
    public static class FactoryEnvironmentResponseBuilder {
    }
}
