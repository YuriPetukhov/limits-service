package com.example.limits.web.mapper;

import com.example.limits.web.dto.CancelResponse;
import com.example.limits.domain.entity.LimitBucket;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class CancelResponseMapper {

    /** Ответ для одиночного bucket’а (scope). */
    public CancelResponse fromBucket(LimitBucket bucket, boolean reverted, String message) {
        if (bucket == null) {
            return new CancelResponse(reverted, message, Map.of());
        }
        BigDecimal remaining = bucket.getRemaining() == null ? BigDecimal.ZERO : bucket.getRemaining();
        String scopeKey = bucket.getScopeKey() == null ? "<unknown>" : bucket.getScopeKey();
        return new CancelResponse(reverted, message, Map.of(scopeKey, remaining));
    }

    /** На будущее: если отмена затрагивает несколько bucket’ов. */
    public CancelResponse fromScopes(boolean reverted, String message, Map<String, BigDecimal> remainingByScope) {
        Map<String, BigDecimal> safe = (remainingByScope == null) ? Map.of() : Map.copyOf(remainingByScope);
        return new CancelResponse(reverted, message, safe);
    }
}
