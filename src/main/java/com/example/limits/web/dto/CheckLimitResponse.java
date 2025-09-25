package com.example.limits.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/** Ответ на проверку лимита. */
@Schema(description = "Результат проверки лимита")
public record CheckLimitResponse(
        @Schema(description = "Достаточно ли средств", example = "true")
        boolean sufficient,

        @Schema(description = "Причина решения", example = "OK")
        String decisionReason,

        @Schema(description = "Прогнозный остаток после списания (если sufficient=true)", example = "9500.00")
        BigDecimal remainingAfter
) {}
