package com.example.limits.application.service.impl;

import com.example.limits.application.service.MaintenanceService;
import com.example.limits.domain.entity.LimitBucket;
import com.example.limits.domain.repository.LimitBucketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Периодическое обслуживание «ведер» лимитов:
 * - находит просроченные окна;
 * - перекатывает окно до актуального момента;
 * - сбрасывает остаток на базовый лимит.
 *
 * Работает пакетами (batch) под блокировкой на уровне репозитория.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {

    private final LimitBucketRepository limitBucketRepository;

    /** Размер выборки на один проход обслуживания (берётся из application.yml). */
    @Value("${limits.scheduler.batch-size:500}")
    private int batchSize;

    /** Максимум итераций за один запуск (safety valve). */
    @Value("${limits.scheduler.max-iterations:1000}")
    private int maxIterations;

    @Override
    @Transactional
    public int sweepExpiredBuckets() {
        int totalProcessed = 0;
        int iteration = 0;
        Instant now = Instant.now();

        while (iteration++ < maxIterations) {
            List<LimitBucket> page = limitBucketRepository.findExpiredForUpdate(now, batchSize);
            if (page.isEmpty()) break;

            for (LimitBucket bucket : page) {
                Long intervalSeconds = bucket.getIntervalSeconds();
                if (intervalSeconds == null) continue;

                long overdueSec = Duration.between(bucket.getNextResetAt(), now).getSeconds();
                long steps = Math.max(1L, (long) Math.ceil((double) overdueSec / intervalSeconds));

                bucket.setLastPeriodStart(bucket.getLastPeriodStart().plusSeconds(steps * intervalSeconds));
                bucket.setNextResetAt(bucket.getNextResetAt().plusSeconds(steps * intervalSeconds));
                bucket.setRemaining(bucket.getBaseLimit());
            }

            limitBucketRepository.saveAll(page);
            totalProcessed += page.size();

            if (page.size() < batchSize) break;
        }

        if (iteration > maxIterations) {
            log.warn("Sweep stopped by max-iterations guard: iterations={}, batchSize={}, processed={}",
                    iteration - 1, batchSize, totalProcessed);
        } else {
            log.info("Sweep done: processed={}, batchSize={}, iterations={}",
                    totalProcessed, batchSize, iteration - 1);
        }

        return totalProcessed;
    }

    /**
     * Перекатывает окно ведра до текущего момента и сбрасывает остаток.
     * Поддерживает два режима:
     * 1) СКОЛЬЗЯЩЕЕ окно: intervalSeconds != null
     * 2) КАЛЕНДАРНОЕ окно: intervalSeconds == null (длина окна = nextResetAt - lastPeriodStart)
     */
    private static void rollWindowAndReset(LimitBucket bucket, Instant now) {
        Instant lastPeriodStart = bucket.getLastPeriodStart();
        Instant nextResetAt = bucket.getNextResetAt();

        if (nextResetAt == null || nextResetAt.isAfter(now)) {
            return;
        }

        Long intervalSeconds = bucket.getIntervalSeconds();

        if (intervalSeconds != null && intervalSeconds > 0) {
            long overdueSeconds = Duration.between(nextResetAt, now).getSeconds();
            long steps = Math.max(1L, (long) Math.ceil((double) overdueSeconds / intervalSeconds));

            long shiftSeconds = steps * intervalSeconds;
            bucket.setLastPeriodStart(lastPeriodStart.plusSeconds(shiftSeconds));
            bucket.setNextResetAt(nextResetAt.plusSeconds(shiftSeconds));
        } else {
            long windowLengthSeconds = Duration.between(lastPeriodStart, nextResetAt).getSeconds();
            if (windowLengthSeconds <= 0) {
                bucket.setLastPeriodStart(now);
                bucket.setNextResetAt(now.plusSeconds(1));
            } else {
                long steps = 0;
                while (!bucket.getNextResetAt().isAfter(now)) {
                    bucket.setLastPeriodStart(bucket.getLastPeriodStart().plusSeconds(windowLengthSeconds));
                    bucket.setNextResetAt(bucket.getNextResetAt().plusSeconds(windowLengthSeconds));
                    steps++;
                }
                if (steps == 0) {
                    // теоретически не попадём сюда, так как входной nextResetAt <= now,
                    // но оставил ветку для наглядности
                }
            }
        }

        bucket.setRemaining(bucket.getBaseLimit());
    }
}
