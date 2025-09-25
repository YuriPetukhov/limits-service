package com.example.limits.domain.policy.matcher.impl;

import com.example.limits.domain.entity.Strategy;
import com.example.limits.domain.policy.matcher.StrategyMatcher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Назначение:
 *  - Матчит стратегию с JSON-спеком формата:
 *    {
 *      "match": {
 *        "all": [ { "attr":"category", "op":"IN", "values":["groceries","fuel"] }, ... ] | true | false,
 *        "any": [ { "attr":"*", "op":"ALWAYS" }, ... ] | true | false
 *      }
 *    }
 *
 * Правила:
 *  - Отсутствие "match" -> стратегия матчится.
 *  - "all": true  -> условие выполняется.
 *  - "all": false -> условие не выполняется.
 *  - "all": [rules] -> все правила в списке должны быть true.
 *  - "any": true       -> условие выполняется.
 *  - "any": [rules]    -> хотя бы одно правило true (пустой список -> true).
 *  - Если и "all", и "any" отсутствуют -> стратегия матчится.
 *
 * Безопасность:
 *  - Невалидный JSON/опкод -> стратегия не матчится.
 */
@Component
@RequiredArgsConstructor
public class JsonStrategyMatcher implements StrategyMatcher {

    private final ObjectMapper objectMapper;

    @Override
    public boolean matches(Strategy strategy, Map<String, String> attributes) {
        Map<String, String> safeAttributes = (attributes == null) ? Map.of() : attributes;

        String specJson = strategy.getSpecJson();
        if (specJson == null || specJson.isBlank()) {
            return true;
        }

        try {
            JsonNode root = objectMapper.readTree(specJson);
            JsonNode matchNode = root.path("match");
            if (matchNode.isMissingNode() || matchNode.isNull()) {
                return true;
            }

            JsonNode anyNode = matchNode.get("any");
            if (anyNode != null && anyNode.isBoolean() && anyNode.booleanValue()) {
                return true;
            }

            boolean allOk = true;
            JsonNode allNode = matchNode.get("all");
            if (allNode != null) {
                if (allNode.isBoolean()) {
                    allOk = allNode.booleanValue(); // true/false
                } else if (allNode.isArray()) {
                    allOk = toStream(allNode).allMatch(ruleNode -> evalRule(ruleNode, safeAttributes));
                }
            }

            boolean anyOk = true;
            if (anyNode != null && anyNode.isArray()) {
                List<JsonNode> anyRules = toList(anyNode);
                anyOk = anyRules.isEmpty() || anyRules.stream().anyMatch(ruleNode -> evalRule(ruleNode, safeAttributes));
            }

            if (allNode == null && anyNode == null) {
                return true;
            }
            return allOk && anyOk;

        } catch (Exception parsingError) {
            return false;
        }
    }

    private static List<JsonNode> toList(JsonNode arrayNode) {
        List<JsonNode> result = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            arrayNode.forEach(result::add);
        }
        return result;
    }

    private static java.util.stream.Stream<JsonNode> toStream(JsonNode arrayNode) {
        return toList(arrayNode).stream();
    }

    private boolean evalRule(JsonNode ruleNode, Map<String, String> attributes) {
        String attributeName = textOrNull(ruleNode.get("attr"));
        String opRaw = textOrNull(ruleNode.get("op"));
        Op op = (opRaw == null) ? Op.ALWAYS : safeOp(opRaw);

        String attributeValue = "*".equals(attributeName) ? "" : attributes.get(attributeName);

        List<String> values = new ArrayList<>();
        JsonNode valuesNode = ruleNode.get("values");
        if (valuesNode != null) {
            if (valuesNode.isArray()) {
                valuesNode.forEach(v -> values.add(textOrNull(v)));
            } else if (valuesNode.isTextual()) {
                values.add(valuesNode.asText());
            }
        }

        return switch (op) {
            case ALWAYS     -> true;
            case EXISTS     -> attributes.containsKey(attributeName) && attributes.get(attributeName) != null;
            case NOT_EXISTS -> !attributes.containsKey(attributeName) || attributes.get(attributeName) == null;
            case EQ         -> attributeValue != null && !values.isEmpty() && attributeValue.equals(values.get(0));
            case NE         -> attributeValue == null || values.isEmpty() || !attributeValue.equals(values.get(0));
            case IN         -> attributeValue != null && !values.isEmpty() && values.contains(attributeValue);
            case REGEX      -> attributeValue != null && !values.isEmpty() && safeRegex(values.get(0), attributeValue);
        };
    }

    private static Op safeOp(String name) {
        try {
            return Op.valueOf(name);
        } catch (Exception ignore) {
            return Op.ALWAYS;
        }
    }

    private static String textOrNull(JsonNode node) {
        return (node == null || node.isNull()) ? null : node.asText();
    }

    private static boolean safeRegex(String pattern, String value) {
        try {
            return Pattern.compile(pattern).matcher(value).matches();
        } catch (Exception e) {
            return false;
        }
    }

    enum Op { ALWAYS, EXISTS, NOT_EXISTS, EQ, NE, IN, REGEX }
}
