package com.example.limits.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;

/** Результат отмены. */
@Schema(description = "Результат отмены транзакции (reversal)")
public record CancelResponse(
        @Schema(description = "Отмена выполнена", example = "true")
        boolean reverted,

        @Schema(description = "Сообщение о результате", example = "OK")
        String resultMessage,

        @Schema(description = "Оставшиеся суммы по scope после отмены", example = """
                {"user:1:category:all:window:day":"10000.00",
                 "user:1:category:all:window:month":"200000.00"}""")
        Map<String, BigDecimal> remainingByScope
) {}
