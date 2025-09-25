package com.example.limits.application.service.handler.debit.impl;

import com.example.limits.domain.exception.NoApplicableStrategyException;
import com.example.limits.application.service.LimitService;
import com.example.limits.application.service.handler.debit.DebitStep;
import com.example.limits.application.service.handler.debit.DebitContext;
import com.example.limits.application.service.handler.debit.ScopePlan;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Шаг 5. Загрузка фактического «уже использовано» по окну/скоупам.
 *
 * На входе: список планов (scope + границы окна).
 * На выходе: в контексте появляется карта usedByScope — израсходованная сумма по каждому scope
 * за текущий период (от periodStart).
 */
@Component
@Order(5)
@RequiredArgsConstructor
public class LoadUsageStep implements DebitStep {

    private final LimitService limitService;

    @Override
    public boolean supports(DebitContext context) {
        return context != null
                && !context.isStopped()
                && context.getPlans() != null
                && !context.getPlans().isEmpty();
    }

    @Override
    public void execute(DebitContext context) {
        if (context.getPlans() == null || context.getPlans().isEmpty()) {
            throw new NoApplicableStrategyException("No applicable policy (empty plan)");
        }

        Instant periodStart = context.getPlans().get(0).periodStart();

        Set<String> scopeKeys = context.getPlans()
                .stream()
                .map(ScopePlan::scopeKey)
                .collect(Collectors.toSet());

        Map<String, BigDecimal> usedByScope = limitService.loadUsed(
                context.getRequest().userId(),
                periodStart,
                scopeKeys
        );

        Map<String, BigDecimal> filledUsedByScope = new HashMap<>(usedByScope);
        for (String scopeKey : scopeKeys) {
            filledUsedByScope.putIfAbsent(scopeKey, BigDecimal.ZERO);
        }

        context.setUsedByScope(filledUsedByScope);
    }
}
