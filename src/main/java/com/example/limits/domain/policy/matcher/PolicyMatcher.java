package com.example.limits.domain.policy.matcher;

import com.example.limits.domain.entity.Strategy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Назначение:
 *  - Отбирает стратегии, которые одновременно:
 *      1) включены (enabled = true);
 *      2) удовлетворяют правилам матчинга по атрибутам через {@link StrategyMatcher}.
 *
 * Контракты:
 *  - На вход можно передавать null/пустые коллекции — в этом случае возвращается пустой список.
 *  - Атрибуты нормализуются на более ранних шагах конвейера; здесь они используются как есть.
 */
@Component
public class PolicyMatcher {

    private final StrategyMatcher strategyMatcher;

    public PolicyMatcher(StrategyMatcher strategyMatcher) {
        this.strategyMatcher = strategyMatcher;
    }

    /**
     * Фильтрует список стратегий по признаку "включена" и по результату {@link StrategyMatcher#matches}.
     *
     * @param strategies           список кандидатов; допускается null/пустой
     * @param requestAttributes    атрибуты запроса (уже нормализованные); допускается null
     * @return неизменяемый список подходящих стратегий
     */
    public List<Strategy> match(List<Strategy> strategies, Map<String, String> requestAttributes) {
        if (strategies == null || strategies.isEmpty()) {
            return List.of();
        }

        Map<String, String> safeAttributes =
                (requestAttributes == null) ? Collections.emptyMap() : requestAttributes;

        List<Strategy> matched = strategies.stream()
                .filter(Strategy::isEnabled)
                .filter(strategy -> strategyMatcher.matches(strategy, safeAttributes))
                .toList();
        return Collections.unmodifiableList(matched);
    }
}
