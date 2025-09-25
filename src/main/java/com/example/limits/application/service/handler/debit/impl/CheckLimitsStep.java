package com.example.limits.application.service.handler.debit.impl;

import com.example.limits.domain.exception.LimitExceededException;
import com.example.limits.application.service.handler.debit.DebitStep;
import com.example.limits.application.service.handler.debit.DebitContext;
import com.example.limits.application.service.handler.debit.ScopePlan;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Шаг 6. Проверка достаточности лимитов.
 *
 * Для каждого плана (scope) считаем остаток ДО списания: limit - used.
 * Если минимальный остаток (бутылочное горлышко) < amount — кидаем LimitExceededException.
 * Иначе готовим карты остатков ДО/ПОСЛЕ и кладём их в контекст.
 */
@Component
@Order(6)
@RequiredArgsConstructor
public class CheckLimitsStep implements DebitStep {

    @Override
    public boolean supports(DebitContext context) {
        return context != null
                && !context.isStopped()
                && context.getPlans() != null
                && !context.getPlans().isEmpty()
                && context.getUsedByScope() != null;
    }

    @Override
    public void execute(DebitContext context) {
        BigDecimal amountToDebit = context.getRequest().amount();

        Map<String, BigDecimal> usedByScope = context.getUsedByScope();
        Map<String, BigDecimal> remainingBeforeByScope = new HashMap<>();
        Map<String, BigDecimal> remainingAfterByScope  = new HashMap<>();

        BigDecimal minimalRemaining = null;     // бутылочное горлышко
        String bottleneckScopeKey   = null;

        for (ScopePlan plan : context.getPlans()) {
            String scopeKey = plan.scopeKey();

            BigDecimal used = usedByScope.getOrDefault(scopeKey, BigDecimal.ZERO);
            BigDecimal remainingBefore = plan.limit().subtract(used); // остаток до списания
            remainingBeforeByScope.put(scopeKey, remainingBefore);

            if (minimalRemaining == null || remainingBefore.compareTo(minimalRemaining) < 0) {
                minimalRemaining = remainingBefore;
                bottleneckScopeKey = scopeKey;
            }
        }

        if (minimalRemaining == null || minimalRemaining.compareTo(amountToDebit) < 0) {
            BigDecimal safeRemaining = (minimalRemaining == null) ? BigDecimal.ZERO : minimalRemaining;
            throw new LimitExceededException(
                    "Insufficient limit in scope=" + bottleneckScopeKey +
                            " (remaining=" + safeRemaining + ", amount=" + amountToDebit + ")"
            );
        }

        for (ScopePlan plan : context.getPlans()) {
            String scopeKey = plan.scopeKey();
            BigDecimal before = remainingBeforeByScope.get(scopeKey);
            remainingAfterByScope.put(scopeKey, before.subtract(amountToDebit));
        }

        context.setRemainingBeforeByScope(remainingBeforeByScope);
        context.setRemainingAfterByScope(remainingAfterByScope);
    }
}
