package com.example.limits.web.mapper;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Считает карту остатков «после списания» для ответа. */
@Component
public class RemainingMapper {

    public Map<String, BigDecimal> after(Map<String, BigDecimal> remainingBeforeByScope,
                                         BigDecimal amount) {
        if (remainingBeforeByScope == null || remainingBeforeByScope.isEmpty()) {
            return Map.of();
        }
        Map<String, BigDecimal> result = new LinkedHashMap<>(remainingBeforeByScope);

        String firstScope = result.keySet().iterator().next();
        BigDecimal before = result.get(firstScope);
        BigDecimal after = before.subtract(amount);
        result.put(firstScope, after);
        return result;
    }
}
