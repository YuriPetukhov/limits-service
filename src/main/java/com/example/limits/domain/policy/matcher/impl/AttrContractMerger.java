package com.example.limits.domain.policy.matcher.impl;

import com.example.limits.domain.policy.contract.AttrContract;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Назначение:
 *  - Объединяет набор спецификаций валидации атрибутов (required/allowed) из разных стратегий
 *    в единый {@link AttrContract}.
 *
 * Правила мерджа:
 *  - required = объединение всех required.
 *  - allowed:
 *      * если ни одна стратегия не указывает allowed -> считаем «разрешены любые ключи»
 *        (в контракте это пустое множество allowedKeys()).
 *      * если хотя бы одна стратегия указала allowed -> разрешены только ключи из объединения allowed.
 *
 * Результат:
 *  - Optional.empty(), если итоговые required и allowed «не заданы» (т.е. нет ограничений).
 *  - Иначе — непустой {@link AttrContract}.
 */
@Component
public final class AttrContractMerger {

    public Optional<AttrContract> merge(List<StrategySpecParser.ValidationSpec> specs) {
        if (specs == null || specs.isEmpty()) {
            return Optional.empty();
        }

        Set<String> requiredKeys = new LinkedHashSet<>();
        Set<String> allowedKeysAccumulated = new LinkedHashSet<>();
        boolean anyAllowedSpecified = false;

        for (StrategySpecParser.ValidationSpec spec : specs) {
            if (spec == null) continue;

            requiredKeys.addAll(spec.required());

            if (spec.hasAllowed()) {
                anyAllowedSpecified = true;
                allowedKeysAccumulated.addAll(spec.allowed());
            }
        }

        if (requiredKeys.isEmpty() && !anyAllowedSpecified) {
            return Optional.empty();
        }

        Set<String> allowedKeys = anyAllowedSpecified ? allowedKeysAccumulated : Set.of();

        AttrContract contract = new AttrContract(
                allowedKeys,
                requiredKeys,
                Map.of(),
                AttrContract.KeyCase.KEEP,
                256,
                Map.of()
        );
        return Optional.of(contract);
    }
}
