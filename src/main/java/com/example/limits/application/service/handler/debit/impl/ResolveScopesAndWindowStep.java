package com.example.limits.application.service.handler.debit.impl;

import com.example.limits.web.dto.TransactionRequest;
import com.example.limits.domain.entity.Strategy;
import com.example.limits.domain.exception.NoApplicableStrategyException;
import com.example.limits.web.mapper.StrategyLimitsMapper;
import com.example.limits.domain.policy.window.WindowSpec;
import com.example.limits.application.service.StrategyService;
import com.example.limits.application.service.handler.debit.DebitStep;
import com.example.limits.application.service.handler.debit.DebitContext;
import com.example.limits.application.service.handler.debit.ScopePlan;
import com.example.limits.domain.policy.window.WindowCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Шаг 4. Разрешение scope и построение планов списания по всем окнам стратегии.
 *
 * На входе: в контексте уже есть список применимых стратегий и нормализованные атрибуты.
 * На выходе: формируем список {@link ScopePlan} для каждого окна каждой стратегии:
 *   - scopeKey: строка ключа (из spec / шаблона; есть fallback user:{userId}:type:{type|all})
 *   - periodStart / nextResetAt: границы окна, рассчитанные {@link WindowCalculator}
 *   - limit: числовой лимит окна
 *   - intervalSeconds: длительность окна в секундах, если окно «скользящее»; для календарных — null
 */
@Slf4j
@Component
@Order(4)
@RequiredArgsConstructor
public class ResolveScopesAndWindowStep implements DebitStep {

    private final StrategyService strategyService;
    private final StrategyLimitsMapper strategyLimitsMapper;

    @Override
    public boolean supports(DebitContext context) {
        return context != null
                && !context.isStopped()
                && context.getPolicies() != null
                && !context.getPolicies().isEmpty();
    }

    @Override
    public void execute(DebitContext context) {
        TransactionRequest request = context.getRequest();
        if (request == null) {
            throw new IllegalStateException("TransactionRequest is not set in DebitContext");
        }

        Instant occurredAt = (request.occurredAt() != null) ? request.occurredAt() : Instant.now();

        Map<String, String> attributes =
                (context.getNormalizedAttributes() != null)
                        ? context.getNormalizedAttributes()
                        : (request.attributes() != null ? request.attributes() : Map.of());

        List<ScopePlan> scopePlans = new ArrayList<>();

        for (Strategy strategy : context.getPolicies()) {

            String scopeKey = strategyService.resolveScope(strategy, request.userId(), attributes);
            if (scopeKey == null || scopeKey.isBlank()) {
                String type = attributes.getOrDefault("type", "all");
                String safeType = (type == null || type.isBlank()) ? "all" : type;
                scopeKey = "user:" + request.userId() + ":type:" + safeType;
                log.debug("Scope fallback applied for strategy id={}, resolved scopeKey={}", strategy.getId(), scopeKey);
            }

            List<WindowSpec> windowSpecs = strategyLimitsMapper.parseWindows(strategy);
            if (windowSpecs == null || windowSpecs.isEmpty()) {
                log.debug("Strategy id={} has no windows; skipping", strategy.getId());
                continue;
            }

            for (WindowSpec windowSpec : windowSpecs) {
                WindowCalculator.WindowBounds bounds = WindowCalculator.boundsFor(windowSpec, occurredAt);

                BigDecimal windowLimit = windowSpec.limit();
                if (windowLimit == null || windowLimit.signum() <= 0) {
                    log.warn("Non-positive or null limit in window (strategy id={}, window={}); skip",
                            strategy.getId(), windowSpec);
                    continue;
                }

                Long intervalSeconds = windowSpec.periodSeconds();

                scopePlans.add(new ScopePlan(
                        scopeKey,
                        bounds.periodStart(),
                        windowLimit,
                        intervalSeconds,
                        bounds.nextResetAt()
                ));
            }
        }

        if (scopePlans.isEmpty()) {
            throw new NoApplicableStrategyException("No applicable policy (empty windows/plans)");
        }

        context.setPlans(scopePlans);
        log.debug("Built {} scope plan(s) for userId={}, txId={}",
                scopePlans.size(), request.userId(), request.txId());
    }
}
