package com.example.limits.web.mapper;

import com.example.limits.web.dto.StrategyResponse;
import com.example.limits.domain.entity.Strategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Маппер стратегии в DTO для API. */
@Component
@RequiredArgsConstructor
public class StrategyMapper {

    private final ObjectMapper objectMapper;

    public StrategyResponse toResponse(Strategy strategy) {
        JsonNode limitsNode = readJsonNode(strategy.getLimitsJson());
        JsonNode specNode   = readJsonNode(strategy.getSpecJson());

        return new StrategyResponse(
                strategy.getId(),
                strategy.getName(),
                strategy.getVersion(),
                strategy.isEnabled(),
                strategy.isDefault(),
                limitsNode,
                emptyToNull(strategy.getDslText()),
                specNode,
                strategy.getCreatedAt(),
                strategy.getUpdatedAt()
        );
    }

    private JsonNode readJsonNode(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return null;
        try {
            return objectMapper.readTree(rawJson);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
