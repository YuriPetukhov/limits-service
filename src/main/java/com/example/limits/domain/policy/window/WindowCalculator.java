package com.example.limits.domain.policy.window;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

public final class WindowCalculator {

    private WindowCalculator() {}

    /** Пара из начала текущего окна и момента следующего сброса. */
    public record WindowBounds(Instant periodStart, Instant nextResetAt) {}

    public static WindowBounds boundsFor(WindowSpec spec, Instant occurredAt) {
        Instant ts = (occurredAt != null) ? occurredAt : Instant.now();

        // Anchor: по умолчанию UTC:00:00
        ZoneId zoneId = ZoneId.of("UTC");
        LocalTime anchorTime = LocalTime.MIDNIGHT;
        if (spec.anchor() != null && !spec.anchor().isBlank() && spec.anchor().contains(":")) {
            String[] parts = spec.anchor().split(":", 2); // "Europe/Moscow" : "HH:mm"
            try {
                zoneId = ZoneId.of(parts[0]);
                anchorTime = LocalTime.parse(parts[1]);
            } catch (Exception ignore) { /* дефолт UTC 00:00 */ }
        }

        ZonedDateTime zdt = ts.atZone(zoneId);

        if (spec.periodSeconds() != null) {
            long seconds = spec.periodSeconds();
            ZonedDateTime todayAnchor = zdt.toLocalDate().atTime(anchorTime).atZone(zoneId);
            if (zdt.isBefore(todayAnchor)) {
                todayAnchor = todayAnchor.minusDays(1);
            }
            long elapsed = Duration.between(todayAnchor, zdt).getSeconds();
            long steps = elapsed / seconds;
            Instant periodStart = todayAnchor.toInstant().plusSeconds(steps * seconds);
            Instant nextReset = periodStart.plusSeconds(seconds);
            return new WindowBounds(periodStart, nextReset);
        }

        String iso = spec.periodIso();
        if ("P1D".equalsIgnoreCase(iso)) {
            ZonedDateTime start = zdt.toLocalDate().atTime(anchorTime).atZone(zoneId);
            if (zdt.isBefore(start)) start = start.minusDays(1);
            return new WindowBounds(start.toInstant(), start.plusDays(1).toInstant());
        }
        if ("P1W".equalsIgnoreCase(iso)) {
            ZonedDateTime startOfWeek = zdt.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .toLocalDate().atTime(anchorTime).atZone(zoneId);
            if (zdt.isBefore(startOfWeek)) startOfWeek = startOfWeek.minusWeeks(1);
            return new WindowBounds(startOfWeek.toInstant(), startOfWeek.plusWeeks(1).toInstant());
        }
        if ("P1M".equalsIgnoreCase(iso)) {
            ZonedDateTime startOfMonth = zdt.with(TemporalAdjusters.firstDayOfMonth())
                    .toLocalDate().atTime(anchorTime).atZone(zoneId);
            if (zdt.isBefore(startOfMonth)) {
                startOfMonth = zdt.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
                        .toLocalDate().atTime(anchorTime).atZone(zoneId);
            }
            return new WindowBounds(startOfMonth.toInstant(), startOfMonth.plusMonths(1).toInstant());
        }

        throw new IllegalArgumentException("Unsupported periodIso: " + iso);
    }
}
