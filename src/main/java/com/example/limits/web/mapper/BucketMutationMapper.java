package com.example.limits.web.mapper;

import com.example.limits.domain.entity.LimitBucket;
import com.example.limits.domain.exception.LimitExceededException;
import com.example.limits.application.service.handler.debit.ScopePlan;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Применяет списание к «ведру». Предполагается, что:
 *  - ведро обеспечено фабрикой для окна (lastPeriodStart совпадает);
 *  - репозиторий читает ведро с PESSIMISTIC_WRITE;
 *  - baseLimit/remaining валидны для данного окна.
 */
@Component
public class BucketMutationMapper {

    public LimitBucket applyDebit(String userId,
                                  LimitBucket bucket,
                                  ScopePlan scopePlan,
                                  Instant periodStart,
                                  BigDecimal amount) {
        if (bucket == null) {
            throw new IllegalStateException("Bucket must be ensured by factory before debit");
        }
        if (!periodStart.equals(bucket.getLastPeriodStart())) {
            throw new IllegalStateException("Bucket window mismatch: expected " + periodStart +
                    " but was " + bucket.getLastPeriodStart());
        }

        // Политика могла поменять лимит — база выравнивается, остаток не корректируем.
        if (bucket.getBaseLimit() == null || bucket.getBaseLimit().compareTo(scopePlan.limit()) != 0) {
            bucket.setBaseLimit(scopePlan.limit());
        }

        BigDecimal remainingBefore = bucket.getRemaining();
        if (remainingBefore.compareTo(amount) < 0) {
            throw new LimitExceededException(
                    "Insufficient limit in scope=" + scopePlan.scopeKey() +
                            " (remaining=" + remainingBefore + ", amount=" + amount + ")"
            );
        }

        bucket.setRemaining(remainingBefore.subtract(amount));
        return bucket;
    }
}
