package com.example.limits.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/** Проведение платежа (дебет лимита). */
@Schema(description = "Запрос на проведение транзакции (дебет лимита)")
public record TransactionRequest(
        @Schema(description = "ID пользователя", example = "1")
        @NotBlank String userId,

        @Schema(description = "Уникальный ID транзакции", example = "tx-2025-09-21-0006")
        @NotBlank String txId,

        @Schema(description = "Сумма списания", example = "125.50")
        @NotNull @Positive BigDecimal amount,

        @Schema(description = "Атрибуты для матчинга стратегии (например, категория, валюта)", example = """
                {"category":"groceries","currency":"RSD"}""")
        Map<String, String> attributes,

        @Schema(description = "Момент совершения (UTC ISO-8601). Если не задан — теперь", example = "2025-09-21T12:11:29Z")
        Instant occurredAt
) {}
