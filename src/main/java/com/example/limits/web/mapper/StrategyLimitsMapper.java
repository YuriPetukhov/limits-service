package com.example.limits.web.mapper;

import com.example.limits.domain.entity.Strategy;
import com.example.limits.domain.policy.window.WindowSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Парсит limits_json в список окон WindowSpec.
 * Поддерживает:
 *  A) плоский объект: { "limit":10000, "periodSeconds":86400, "anchor":"UTC:00:00" }
 *  B) {"windows":[ { ... }, { ... } ]}
 */
@Component
@RequiredArgsConstructor
public class StrategyLimitsMapper {

    private final ObjectMapper objectMapper;

    public List<WindowSpec> parseWindows(Strategy strategy) {
        String raw = strategy.getLimitsJson();
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("limits_json is blank for strategy id=" + strategy.getId());
        }
        try {
            JsonNode root = objectMapper.readTree(raw);

            // Вариант B: объект с "windows": [...]
            if (root.isObject() && root.has("windows") && root.get("windows").isArray()) {
                List<WindowSpec> result = new ArrayList<>();
                for (JsonNode node : root.get("windows")) {
                    WindowSpec spec = toWindowSpec(node);
                    if (spec != null) result.add(spec);
                }
                if (result.isEmpty()) {
                    throw new IllegalArgumentException("limits_json has empty windows[] for strategy id=" + strategy.getId());
                }
                return result;
            }

            // Вариант A: плоский объект
            if (root.isObject()) {
                WindowSpec single = toWindowSpec(root);
                if (single == null) {
                    throw new IllegalArgumentException("limits_json has no valid window for strategy id=" + strategy.getId());
                }
                return List.of(single);
            }

            throw new IllegalArgumentException("limits_json root must be object for strategy id=" + strategy.getId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid limits_json for strategy id=" + strategy.getId(), e);
        }
    }

    private WindowSpec toWindowSpec(JsonNode node) {
        BigDecimal limit = readBigDecimal(node.get("limit"));
        if (limit == null || limit.signum() <= 0) return null;

        String id = readText(node.get("id"));
        String anchor = readText(node.get("anchor"));

        Long periodSeconds = readLong(node.get("periodSeconds"));
        String periodIso = readText(node.get("periodIso"));

        if (periodSeconds == null && (periodIso == null || periodIso.isBlank())) {
            return null;
        }
        return new WindowSpec(id, limit, periodSeconds, periodIso, anchor);
    }

    private static String readText(JsonNode node) {
        return (node != null && node.isTextual()) ? node.asText().trim() : null;
    }
    private static Long readLong(JsonNode node) {
        return (node != null && node.isNumber()) ? node.longValue() : null;
    }
    private static BigDecimal readBigDecimal(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isNumber()) return node.decimalValue();
        if (node.isTextual()) {
            String txt = node.asText().trim();
            if (txt.isEmpty()) return null;
            try { return new BigDecimal(txt); } catch (Exception ignore) { return null; }
        }
        return null;
    }
}
