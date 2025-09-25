package com.example.limits.web.dto;

import com.example.limits.web.dto.enums.DecisionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;

/** Результат проведения платежа. */
@Schema(description = "Результат транзакции (дебет)")
public record TransactionResponse(
        @Schema(description = "Статус решения", example = "APPROVED")
        DecisionStatus status,

        @Schema(description = "Причина решения (если отклонено)", example = "INSUFFICIENT_FUNDS")
        String decisionReason,

        @Schema(description = "Фактически списанная сумма", example = "125.50")
        BigDecimal debitedAmount,

        @Schema(description = "Оставшиеся суммы по каждой «коробочке» лимита (scope)", example = """
                {"user:1:category:all:window:day": "9874.50",
                 "user:1:category:all:window:month": "199874.50"}""")
        Map<String, BigDecimal> remainingByScope
) {}
