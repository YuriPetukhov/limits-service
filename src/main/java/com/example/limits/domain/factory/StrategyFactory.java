package com.example.limits.domain.factory;

import com.example.limits.web.dto.CreateStrategyRequest;
import com.example.limits.domain.entity.Strategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Фабрика стратегий.
 * Отвечает за сборку сущности Strategy из входных DTO без «обогащения» бизнес-логикой.
 * JSON-поля (limits/spec) сохраняются в БД как есть (jsonb), сериализуются через ObjectMapper.
 */
@Component
@RequiredArgsConstructor
public class StrategyFactory {

    private final ObjectMapper objectMapper;

    /**
     * Построить Strategy по запросу на создание.
     *
     * @param request             входной запрос
     * @param normalizedEnabled   флаг enabled после нормализации (может быть null → не трогаем поле)
     * @param normalizedDefault   флаг isDefault после нормализации (может быть null → не трогаем поле)
     * @return готовая сущность для сохранения
     */
    public Strategy buildFromRequest(CreateStrategyRequest request,
                                     Boolean normalizedEnabled,
                                     Boolean normalizedDefault) {
        Strategy strategy = new Strategy();
        strategy.setName(request.name());
        strategy.setVersion(request.version());

        if (normalizedEnabled != null) {
            strategy.setEnabled(normalizedEnabled);
        }
        if (normalizedDefault != null) {
            strategy.setDefault(normalizedDefault);
        }

        strategy.setDslText(emptyToNull(request.dslText()));

        if (request.spec() != null) {
            strategy.setSpecJson(writeAsString(request.spec()));
        }
        if (request.limits() != null) {
            strategy.setLimitsJson(writeAsString(request.limits()));
        }
        return strategy;
    }

    /**
     * Удобный конструктор «дефолтной глобальной дневной» стратегии.
     * Для дев-сценариев.
     */
    public Strategy buildGlobalDailyDefault(String name,
                                            int version,
                                            java.math.BigDecimal dailyLimit) {
        Strategy strategy = new Strategy();
        strategy.setName(name);
        strategy.setVersion(version);
        strategy.setEnabled(true);
        strategy.setDefault(true);
        strategy.setDslText(null);

        String limitsJson = """
            {
              "windows": [
                { "id": "day", "periodIso": "P1D", "anchor": "UTC:00:00", "limit": %s }
              ]
            }
            """.formatted(dailyLimit);
        strategy.setLimitsJson(limitsJson);

        String specJson = """
            {
              "match": { "any": [ { "op": "ALWAYS" } ] },
              "scopeTemplate": "user:${userId}:type:${type:-all}"
            }
            """;
        strategy.setSpecJson(specJson);

        return strategy;
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private String writeAsString(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid JSON payload", exception);
        }
    }
}
