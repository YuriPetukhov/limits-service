package com.example.limits.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Утилиты для работы с JSON. */
public final class Jsons {
    private Jsons() {}

    public static boolean equals(ObjectMapper om, String leftJson, Object rightObject) {
        if (rightObject == null) {
            return leftJson == null || leftJson.isBlank();
        }
        if (leftJson == null || leftJson.isBlank()) {
            return false;
        }
        try {
            JsonNode left = om.readTree(leftJson);
            JsonNode right = om.valueToTree(rightObject);
            return left.equals(right);
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /** Фолбэк-версия без явного ObjectMapper. */
    public static boolean equals(String leftJson, Object rightObject) {
        return equals(DefaultMapperHolder.MAPPER, leftJson, rightObject);
    }

    /** Ленивая инициализация дефолтного ObjectMapper с модулями. */
    private static final class DefaultMapperHolder {
        static final ObjectMapper MAPPER = new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
                .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, false);
    }
}
