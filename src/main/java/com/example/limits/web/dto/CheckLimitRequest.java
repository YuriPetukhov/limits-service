package com.example.limits.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/** Проверка доступности лимита без списания. */
@Schema(description = "Проверка доступного лимита (без списания)")
public record CheckLimitRequest(
        @Schema(description = "ID пользователя", example = "1")
        @NotBlank String userId,

        @Schema(description = "Планируемая сумма списания", example = "500.00")
        @NotNull @Positive BigDecimal amount,

        @Schema(description = "Атрибуты (например, категория)", example = """
                {
                "category":"groceries"
                }
                """)
        Map<String, String> attributes,

        @Schema(description = "Момент совершения (UTC ISO-8601). Если не задан — теперь", example = "2025-09-21T12:11:29Z")
        Instant occurredAt
) {}
