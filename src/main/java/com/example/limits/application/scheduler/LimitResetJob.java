package com.example.limits.application.scheduler;

import com.example.limits.application.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Периодический сброс просроченных окон лимитов (limit_bucket.next_reset_at).
 * Выполняет «ленивую» ротацию бакетов батчами внутри MaintenanceService.
 *
 * Управление:
 *  - включение: limits.scheduler.enabled=true (по умолчанию true)
 *  - расписание: limits.scheduler.cron (по умолчанию "0 0 0 * * *")
 *  - часовой пояс: limits.scheduler.zone (по умолчанию "Europe/Moscow")
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "limits.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LimitResetJob {

    private final MaintenanceService maintenanceService;

    @Value("${limits.scheduler.cron:0 0 0 * * *}")
    private String cronForLog;

    @Scheduled(
            cron = "${limits.scheduler.cron:0 0 0 * * *}",
            zone = "${limits.scheduler.zone:Europe/Moscow}"
    )
    public void runSweep() {
        long started = System.currentTimeMillis();
        try {
            int resetCount = maintenanceService.sweepExpiredBuckets();
            long tookMs = System.currentTimeMillis() - started;
            log.info("LimitResetJob finished: resetCount={}, took={}ms, cron={}", resetCount, tookMs, cronForLog);
        } catch (Exception ex) {
            log.error("LimitResetJob failed (cron={})", cronForLog, ex);
        }
    }
}
