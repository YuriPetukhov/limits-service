package com.example.limits.web.mapper;

import com.example.limits.domain.entity.Strategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Собирает scopeKey по шаблону из spec_json.scopeTemplate.
 * Поддерживает плейсхолдеры:
 *   ${userId}, ${attr}, ${attr:-defaultValue}
 */
@Component
@RequiredArgsConstructor
public class ScopeMapper {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}:]+)(?::-(.+?))?}"); // ${key} или ${key:-def}

    private final ObjectMapper objectMapper;

    public String resolveScope(Strategy strategy, String userId, Map<String, String> attributes) {
        String rawJson = strategy.getSpecJson();
        String template = null;
        try {
            if (rawJson != null && !rawJson.isBlank()) {
                JsonNode root = objectMapper.readTree(rawJson);
                JsonNode tmpl = root.path("scopeTemplate");
                if (tmpl.isTextual()) {
                    template = tmpl.asText();
                }
            }
        } catch (Exception ignore) {
            // битый JSON — игнор: фоллбек
        }

        if (template == null || template.isBlank()) {
            String type = attributes != null ? attributes.getOrDefault("type", "all") : "all";
            return "user:" + userId + ":type:" + (type == null || type.isBlank() ? "all" : type);
        }

        Map<String, String> safeAttrs = (attributes == null) ? Map.of() : attributes;
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String defaultValue = matcher.group(2);

            String value;
            if ("userId".equals(key)) {
                value = userId;
            } else {
                String candidate = safeAttrs.get(key);
                value = (candidate == null || candidate.isBlank())
                        ? (defaultValue != null ? defaultValue : "")
                        : candidate;
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
