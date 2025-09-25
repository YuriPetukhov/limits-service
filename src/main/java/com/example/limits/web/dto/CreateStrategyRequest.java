package com.example.limits.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** Создание/регистрация стратегии (политики). */
@Schema(description = "Создание стратегии лимитов")
public record CreateStrategyRequest(
        @Schema(description = "Имя стратегии", example = "GLOBAL_DAILY_MONTHLY")
        @NotBlank String name,

        @Schema(description = "Версия", example = "1")
        @NotNull Integer version,

        @Schema(description = "Включена ли стратегия", example = "true")
        Boolean enabled,

        @Schema(description = "Сделать дефолтной", example = "false")
        Boolean isDefault,

        @Schema(description = "Описание лимитов (произвольный JSON). См. формат windows[] или daily/monthly", example = """
                {"windows":[
                  {"id":"day","periodIso":"P1D","anchor":"Europe/Moscow:00:00","limit":10000},
                  {"id":"month","periodIso":"P1M","anchor":"Europe/Moscow:00:00","limit":200000}
                ]}""")
        JsonNode limits,

        @Schema(description = "Спецификация матчинга/скоупа (произвольный JSON)", example = """
                {"scopeTemplate":"user:${userId}:category:${category:-all}",
                 "match":{"any":[{"op":"ALWAYS"}]}}""")
        JsonNode spec,

        @Schema(description = "Опциональный DSL-текст", example = "strategy ...")
        String dslText
) {}
