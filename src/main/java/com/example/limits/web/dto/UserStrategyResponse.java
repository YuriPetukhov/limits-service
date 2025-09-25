package com.example.limits.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** Текущая/созданная привязка стратегии к пользователю. */
@Schema(description = "Привязка стратегии к пользователю")
public record UserStrategyResponse(
        @Schema(description = "ID записи привязки", example = "101")
        Long id,

        @Schema(description = "ID пользователя", example = "1")
        String userId,

        @Schema(description = "ID стратегии", example = "42")
        Long strategyId,

        @Schema(description = "Имя стратегии", example = "GLOBAL_DAILY_MONTHLY")
        String strategyName,

        @Schema(description = "Версия стратегии", example = "1")
        Integer strategyVersion,

        @Schema(description = "Активна ли привязка", example = "true")
        boolean isActive,

        @Schema(description = "Действует с (UTC ISO-8601)", example = "2025-10-01T00:00:00Z")
        Instant effectiveFrom,

        @Schema(description = "Действует до (UTC ISO-8601)", example = "null")
        Instant effectiveTo,

        @Schema(description = "Создано (UTC ISO-8601)")
        Instant createdAt,

        @Schema(description = "Обновлено (UTC ISO-8601)")
        Instant updatedAt
) {}
