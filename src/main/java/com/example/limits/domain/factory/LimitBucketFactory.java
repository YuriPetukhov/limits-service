package com.example.limits.domain.factory;

import com.example.limits.domain.entity.LimitBucket;
import com.example.limits.domain.repository.LimitBucketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Создаёт/обновляет лимитный «бакет» под конкретное окно.
 * ВАЖНО: intervalSeconds может быть null (календарное окно) — тогда опираемся на nextResetAt.
 */
@Component
@RequiredArgsConstructor
public class LimitBucketFactory {

    private final LimitBucketRepository bucketRepo;

    /**
     * Гарантирует наличие ведра на заданное окно.
     * Если окно новое — создаёт.
     * Если ведро есть и начался новый период — сбрасывает remaining до base и переставляет период.
     */
    public LimitBucket ensureForWindow(String userId,
                                       String scopeKey,
                                       Instant periodStart,
                                       BigDecimal baseLimit,
                                       Long intervalSeconds,
                                       Instant nextResetAt) {

        Optional<LimitBucket> existingOpt = bucketRepo.findByUserIdAndScopeKey(userId, scopeKey);

        if (existingOpt.isEmpty()) {
            LimitBucket bucket = new LimitBucket();
            bucket.setUserId(userId);
            bucket.setScopeKey(scopeKey);
            bucket.setBaseLimit(baseLimit);
            bucket.setRemaining(baseLimit);
            bucket.setIntervalSeconds(intervalSeconds);
            bucket.setLastPeriodStart(periodStart);
            bucket.setNextResetAt(nextResetAt);
            return bucketRepo.save(bucket);
        }

        LimitBucket bucket = existingOpt.get();

        // Если пришло окно позже текущего — считаем, что начался новый период.
        if (!periodStart.equals(bucket.getLastPeriodStart())) {
            bucket.setLastPeriodStart(periodStart);
            bucket.setBaseLimit(baseLimit);
            bucket.setRemaining(baseLimit);
        } else {
            // В этом же периоде можно подстроить базовый лимит (например, политика изменилась)
            if (bucket.getBaseLimit() == null || bucket.getBaseLimit().compareTo(baseLimit) != 0) {
                bucket.setBaseLimit(baseLimit);
                if (bucket.getRemaining().compareTo(baseLimit) > 0) {
                    bucket.setRemaining(baseLimit);
                }
            }
        }

        bucket.setIntervalSeconds(intervalSeconds);
        bucket.setNextResetAt(nextResetAt);

        return bucketRepo.save(bucket);
    }
}
