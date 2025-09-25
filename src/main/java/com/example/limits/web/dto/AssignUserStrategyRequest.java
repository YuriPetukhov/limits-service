package com.example.limits.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/** Привязка стратегии к пользователю (создание/изменение). */
@Schema(description = "Назначение (или смена) активной стратегии для пользователя")
public record AssignUserStrategyRequest(
        @Schema(description = "ID стратегии", example = "42")
        @NotNull Long strategyId,

        @Schema(description = "Активировать привязку (по умолчанию true)", example = "true")
        Boolean isActive,

        @Schema(description = "Действует с (UTC ISO-8601)", example = "2025-10-01T00:00:00Z")
        Instant effectiveFrom,

        @Schema(description = "Действует до (UTC ISO-8601)", example = "null")
        Instant effectiveTo
) {}
