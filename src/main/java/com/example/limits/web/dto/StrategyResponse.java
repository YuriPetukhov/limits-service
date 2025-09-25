package com.example.limits.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** Отображение стратегии. */
@Schema(description = "Стратегия лимитов")
public record StrategyResponse(
        @Schema(description = "ID стратегии", example = "42")
        Long id,

        @Schema(description = "Имя стратегии", example = "GLOBAL_DAILY_MONTHLY")
        String name,

        @Schema(description = "Версия", example = "1")
        Integer version,

        @Schema(description = "Включена ли", example = "true")
        boolean enabled,

        @Schema(description = "Дефолтная ли", example = "false")
        boolean isDefault,

        @Schema(
                description = "Описание лимитов (JSON). См. формат windows[] или плоский объект",
                example = """
            {
              "windows": [
                {"id":"day","periodIso":"P1D","anchor":"UTC:00:00","limit":10000},
                {"id":"month","periodIso":"P1M","anchor":"UTC:00:00","limit":200000}
              ]
            }
            """
        )
        JsonNode limits,

        @Schema(description = "DSL-описание (опционально)")
        String dslText,

        @Schema(
                description = "Спецификация матчинга/скоупа (произвольный JSON)",
                example = """
            {
              "scopeTemplate": "user:${userId}:type:${type:-all}",
              "match": { "any": [ { "op": "ALWAYS" } ] }
            }
            """
        )
        JsonNode spec,

        @Schema(description = "Создано (UTC ISO-8601)")
        Instant createdAt,

        @Schema(description = "Обновлено (UTC ISO-8601)")
        Instant updatedAt
) {}
