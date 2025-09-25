package com.example.limits.web.mapper;

import com.example.limits.domain.entity.LimitBucket;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Считает использованную сумму в текущем окне по «ведру».
 * Если окно ведра не совпадает с переданным periodStart — считаем, что в этом окне ещё не тратили.
 */
@Component
public class BucketUsageMapper {

    public BigDecimal usedForPeriod(LimitBucket bucket, Instant periodStart) {
        if (bucket == null) return BigDecimal.ZERO;
        if (!periodStart.equals(bucket.getLastPeriodStart())) return BigDecimal.ZERO;

        BigDecimal base = bucket.getBaseLimit() != null ? bucket.getBaseLimit() : BigDecimal.ZERO;
        BigDecimal remaining = bucket.getRemaining() != null ? bucket.getRemaining() : BigDecimal.ZERO;
        BigDecimal used = base.subtract(remaining);
        return used.signum() < 0 ? BigDecimal.ZERO : used;
    }
}
