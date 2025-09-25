package com.example.limits.application.service.handler.strategy.create.impl;

import com.example.limits.application.service.handler.strategy.create.CreateStrategyContext;
import com.example.limits.application.service.handler.strategy.create.CreateStrategyStep;
import com.example.limits.web.dto.CreateStrategyRequest;
import com.example.limits.web.dto.StrategyResponse;
import com.example.limits.domain.entity.Strategy;
import com.example.limits.domain.exception.ValidationConflictException;
import com.example.limits.web.mapper.StrategyMapper;
import com.example.limits.domain.repository.StrategyRepository;
import com.example.limits.util.json.Jsons;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Проверяет уникальность (name, version).
 * Если найдено совпадение и содержимое эквивалентно — идемпотентный ответ.
 * Иначе — 409 Conflict.
 */
@Component
@Order(3)
@RequiredArgsConstructor
public class UniquenessAndIdempotencyStep implements CreateStrategyStep {

    private final StrategyRepository strategyRepository;
    private final StrategyMapper strategyMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void execute(CreateStrategyContext context) {
        CreateStrategyRequest request = context.getRequest();

        Optional<Strategy> existingOpt =
                strategyRepository.findByNameAndVersion(request.name(), request.version());

        if (existingOpt.isEmpty()) {
            return; // нет конфликта — продолжим создание
        }

        Strategy existing = existingOpt.get();

        boolean sameEnabled = (context.getNormalizedEnabled() == null)
                ? existing.isEnabled()
                : existing.isEnabled() == context.getNormalizedEnabled();

        boolean sameDefault = (context.getNormalizedDefault() == null)
                ? existing.isDefault()
                : existing.isDefault() == context.getNormalizedDefault();

        boolean sameDsl     = equalsNullSafe(existing.getDslText(), request.dslText());
        boolean sameSpec    = Jsons.equals(objectMapper, existing.getSpecJson(),   request.spec());
        boolean sameLimits  = Jsons.equals(objectMapper, existing.getLimitsJson(), request.limits());

        if (sameEnabled && sameDefault && sameDsl && sameSpec && sameLimits) {
            StrategyResponse response = strategyMapper.toResponse(existing);
            context.setCreated(false);
            context.setResourceId(String.valueOf(existing.getId()));
            context.stopWith(response);
            return;
        }

        throw new ValidationConflictException(
                "Strategy %s v%d already exists with different payload"
                        .formatted(request.name(), request.version())
        );
    }

    private static boolean equalsNullSafe(String a, String b) {
        if (a == null || a.isBlank()) return (b == null || b.isBlank());
        if (b == null || b.isBlank()) return false;
        return a.equals(b);
    }
}
