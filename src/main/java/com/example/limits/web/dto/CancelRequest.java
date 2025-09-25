package com.example.limits.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.Map;

/** Отмена ранее проведённой транзакции (reversal). */
@Schema(description = "Запрос на отмену ранее проведённой транзакции (reversal)")
public record CancelRequest(
        @Schema(description = "ID пользователя", example = "1")
        @NotBlank String userId,

        @Schema(description = "ID новой операции-отмены", example = "tx-001-rev")
        @NotBlank String txId,

        @Schema(description = "ID исходной транзакции, которую отменяем", example = "tx-001")
        @NotBlank String originalTxId,

        @Schema(description = "Момент регистрации отмены (UTC ISO-8601). Если не задан — теперь", example = "2025-09-21T12:20:00Z")
        Instant occurredAt,

        @Schema(description = "Атрибуты (опционально)", example = "{}")
        Map<String, String> attributes
) {}
