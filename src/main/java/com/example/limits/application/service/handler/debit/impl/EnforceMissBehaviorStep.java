package com.example.limits.application.service.handler.debit.impl;

import com.example.limits.config.LimitsProperties;
import com.example.limits.web.dto.TransactionRequest;
import com.example.limits.domain.entity.Strategy;
import com.example.limits.config.enums.MissBehavior;
import com.example.limits.domain.exception.NoApplicableStrategyException;
import com.example.limits.application.service.StrategyService;
import com.example.limits.application.service.handler.debit.DebitStep;
import com.example.limits.application.service.handler.debit.DebitContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Шаг 3. Поведение при отсутствии подходящих политик (miss-behavior).
 *
 * Если предыдущий шаг не подобрал ни одной стратегии:
 *  - при REJECT — выбрасываем бизнес-исключение (это уйдёт как 422/400 в зависимости от маппинга),
 *  - при USE_DEFAULT — подбираем лучшую дефолтную стратегию и продолжаем пайплайн.
 */
@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class EnforceMissBehaviorStep implements DebitStep {

    private final StrategyService strategyService;
    private final LimitsProperties limitsProperties;

    @Override
    public boolean supports(DebitContext context) {
        return context != null
                && !context.isStopped()
                && (context.getPolicies() == null || context.getPolicies().isEmpty());
    }

    @Override
    public void execute(DebitContext context) {
        MissBehavior behavior = limitsProperties.getMissBehavior();
        if (behavior == MissBehavior.REJECT) {
            throw new NoApplicableStrategyException("No applicable policy for request");
        }

        TransactionRequest request = context.getRequest();
        String userId = request.userId();
        Instant occurredAt = request.occurredAt();
        Map<String, String> normalizedAttributes =
                (context.getNormalizedAttributes() != null)
                        ? context.getNormalizedAttributes()
                        : (request.attributes() != null ? request.attributes() : Map.of());

        Strategy defaultStrategy = strategyService
                .resolveBestDefault(userId, normalizedAttributes, occurredAt)
                .orElseThrow(() -> new IllegalStateException("Default policy is missing"));

        log.debug("Fallback to default policy: id={}, name={}, v={}",
                defaultStrategy.getId(), defaultStrategy.getName(), defaultStrategy.getVersion());

        context.setPolicies(List.of(defaultStrategy));
        context.setFallbackUsed(true);
    }
}
