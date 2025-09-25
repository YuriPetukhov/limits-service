package com.example.limits.web.mapper;

import com.example.limits.web.dto.CheckLimitResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CheckLimitResponseMapper {

    /** sufficient=true + расчёт remainingAfter = totalRemaining - amount */
    public CheckLimitResponse ok(BigDecimal totalRemaining, BigDecimal amount) {
        return new CheckLimitResponse(
                true,
                "OK",
                totalRemaining.subtract(amount)
        );
    }

    /** sufficient=false: оставляем текущий totalRemaining как есть */
    public CheckLimitResponse insufficient(BigDecimal totalRemaining) {
        return new CheckLimitResponse(
                false,
                "INSUFFICIENT_FUNDS",
                totalRemaining
        );
    }

    /** Универсальный вариант, если уже посчитан флаг и remainingAfter. */
    public CheckLimitResponse of(boolean sufficient, String reason, BigDecimal remainingAfter) {
        return new CheckLimitResponse(sufficient, reason, remainingAfter);
    }
}
