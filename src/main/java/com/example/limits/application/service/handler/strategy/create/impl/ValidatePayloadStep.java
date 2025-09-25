package com.example.limits.application.service.handler.strategy.create.impl;

import com.example.limits.application.service.handler.strategy.create.CreateStrategyContext;
import com.example.limits.application.service.handler.strategy.create.CreateStrategyStep;
import com.example.limits.web.dto.CreateStrategyRequest;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Базовая валидация: обязательные name/version и наличие хотя бы одного
 * из полей limits / dslText / spec.
 *
 * ВАЖНО: здесь мы не проводим глубокую проверку структуры JSON (limits/spec),
 * чтобы не завязывать CRUD на конкретную бизнес-логику. Глубокая валидация —
 * в резолверах/матчерах во время использования стратегии.
 */
@Component
@Order(2)
public class ValidatePayloadStep implements CreateStrategyStep {

    @Override
    public void execute(CreateStrategyContext context) {
        CreateStrategyRequest request = context.getRequest();

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (request.version() == null || request.version() <= 0) {
            throw new IllegalArgumentException("version must be positive");
        }

        boolean hasLimits = hasNonEmptyJson(request.limits());
        boolean hasDsl    = request.dslText() != null && !request.dslText().isBlank();
        boolean hasSpec   = hasNonEmptyJson(request.spec());

        if (!hasLimits && !hasDsl && !hasSpec) {
            throw new IllegalArgumentException("Provide at least one of: limits, dslText, spec");
        }
    }

    private static boolean hasNonEmptyJson(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isMissingNode() || jsonNode.isNull()) {
            return false;
        }
        // Если объект — проверим, что есть хотя бы одно поле; для массивов/скаляров — достаточно самого факта наличия
        if (jsonNode.isObject()) {
            return jsonNode.fieldNames().hasNext();
        }
        return true;
    }
}
