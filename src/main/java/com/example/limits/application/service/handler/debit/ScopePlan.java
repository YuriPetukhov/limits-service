package com.example.limits.application.service.handler.debit;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * План списания по одному окну лимита (scope + окно).
 * <p>
 * Используется конвейером debit для:
 * <ul>
 *   <li>подготовки/переката ведра (bucket) под нужное окно,</li>
 *   <li>расчёта доступных остатков,</li>
 *   <li>самого списания и логирования транзакции.</li>
 * </ul>
 *
 * <h3>Правила по полям</h3>
 * <ul>
 *   <li>{@code scopeKey} — ключ скоупа (например, {@code user:123:type:all}).</li>
 *   <li>{@code periodStart} — начало текущего окна (в UTC/Instant).</li>
 *   <li>{@code limit} — базовый лимит окна (&gt; 0).</li>
 *   <li>{@code intervalSeconds} — длительность окна в секундах для «скользящих/фиксированных» окон.
 *       Для календарных окон (день/месяц и т.п.) — {@code null}.</li>
 *   <li>{@code nextResetAt} — момент следующего сброса/переката окна (обязателен всегда).
 *       Должен быть позже {@code periodStart}.</li>
 * </ul>
 */
public record ScopePlan(
        String scopeKey,
        Instant periodStart,
        BigDecimal limit,
        Long intervalSeconds,
        Instant nextResetAt
) {
    /**
     * Конструктор с валидацией инвариантов.
     */
    public ScopePlan {
        if (scopeKey == null || scopeKey.isBlank()) {
            throw new IllegalArgumentException("scopeKey must not be null/blank");
        }
        if (periodStart == null) {
            throw new IllegalArgumentException("periodStart must not be null");
        }
        if (limit == null || limit.signum() <= 0) {
            throw new IllegalArgumentException("limit must be > 0");
        }
        if (intervalSeconds != null && intervalSeconds <= 0L) {
            throw new IllegalArgumentException("intervalSeconds must be > 0 when provided");
        }
        if (nextResetAt == null) {
            throw new IllegalArgumentException("nextResetAt must not be null");
        }
        if (!nextResetAt.isAfter(periodStart)) {
            throw new IllegalArgumentException("nextResetAt must be after periodStart");
        }
    }

    /**
     * Фабрика для календарного окна (day/week/month/…):
     * {@code intervalSeconds} будет {@code null}.
     */
    public static ScopePlan calendarWindow(String scopeKey,
                                           Instant periodStart,
                                           BigDecimal limit,
                                           Instant nextResetAt) {
        return new ScopePlan(scopeKey, periodStart, limit, null, nextResetAt);
    }

    /**
     * Фабрика для фиксированного окна заданной длины (в секундах).
     */
    public static ScopePlan fixedWindow(String scopeKey,
                                        Instant periodStart,
                                        BigDecimal limit,
                                        long intervalSeconds,
                                        Instant nextResetAt) {
        return new ScopePlan(scopeKey, periodStart, limit, intervalSeconds, nextResetAt);
    }
}
