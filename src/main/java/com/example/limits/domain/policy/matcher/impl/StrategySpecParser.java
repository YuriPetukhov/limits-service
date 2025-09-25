package com.example.limits.domain.policy.matcher.impl;

import com.example.limits.domain.entity.Strategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Назначение:
 *  - Достаёт из spec_json раздел validation.* (required/allowed), чтобы затем собрать атрибутный контракт.
 *
 * Ожидаемый фрагмент JSON:
 *  {
 *    "validation": {
 *      "requiredAttrs": ["userId", "category"],
 *      "allowedAttrs":  ["userId", "category", "currency"]
 *    }
 *  }
 *
 * Устойчивость:
 *  - Любые ошибки парсинга приводят к возврату ValidationSpec.empty().
 *  - Отсутствие validation-блока -> ValidationSpec.empty().
 */
@Component
@RequiredArgsConstructor
public class StrategySpecParser {

    private final ObjectMapper objectMapper;

    /** Извлекает validation.requiredAttrs и validation.allowedAttrs. Ошибки JSON — игнорируем. */
    public ValidationSpec parseValidation(Strategy strategy) {
        String specJson = strategy.getSpecJson();
        if (specJson == null || specJson.isBlank()) {
            return ValidationSpec.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(specJson);
            JsonNode validationNode = root.get("validation");
            if (validationNode == null || !validationNode.isObject()) {
                return ValidationSpec.empty();
            }

            Set<String> required = readStringArray(validationNode.get("requiredAttrs"));
            Set<String> allowed  = readStringArray(validationNode.get("allowedAttrs"));
            boolean hasAllowed   = validationNode.has("allowedAttrs") && validationNode.get("allowedAttrs").isArray();

            return new ValidationSpec(required, allowed, hasAllowed);
        } catch (Exception ignore) {
            return ValidationSpec.empty();
        }
    }

    private static Set<String> readStringArray(JsonNode node) {
        Set<String> result = new LinkedHashSet<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                if (item.isTextual()) {
                    result.add(item.asText());
                }
            }
        }
        return result;
    }

    /**
     * Набор правил валидации, извлечённых из spec_json.
     *
     * @param required   обязательные ключи атрибутов
     * @param allowed    разрешённые ключи атрибутов
     * @param hasAllowed признак того, что allowed задавался хоть где-то (важно для мерджа)
     */
    public record ValidationSpec(Set<String> required, Set<String> allowed, boolean hasAllowed) {
        public static ValidationSpec empty() { return new ValidationSpec(Set.of(), Set.of(), false); }
    }
}
