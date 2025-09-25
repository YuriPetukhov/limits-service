package com.example.limits.application.service.handler.cancel.impl;

import com.example.limits.application.service.handler.cancel.CancelContext;
import com.example.limits.application.service.handler.cancel.CancelStep;
import com.example.limits.domain.entity.LimitBucket;
import com.example.limits.domain.exception.ValidationConflictException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Шаг 3. Проверка бизнес-правила: отменять можно только в пределах текущего окна бакета.
 * Т.е. now ∈ [last_period_start, next_reset_at).
 */
@Component
@Order(3)
public class CheckReversalPolicyStep implements CancelStep {

    @Override
    public void execute(CancelContext context) {
        LimitBucket bucket = context.getBucket();
        Instant now = Instant.now();

        boolean beforeWindowStart = now.isBefore(bucket.getLastPeriodStart());
        boolean afterOrAtWindowEnd = !now.isBefore(bucket.getNextResetAt()); // now >= next_reset_at

        if (beforeWindowStart || afterOrAtWindowEnd) {
            throw new ValidationConflictException("Reversal is allowed only within the current window");
        }
    }
}
