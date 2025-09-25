package com.example.limits.domain.policy.window;

import java.math.BigDecimal;

public record WindowSpec(
        String id,              // опционально: "daily", "monthly", "3d" и т.п.
        BigDecimal limit,       // обязательный лимит
        Long periodSeconds,     // DURATION-окно: длительность в секундах (например, 86400)
        String periodIso,       // CALENDAR-окно: "P1D"/"P1W"/"P1M" и т.п. (если задано, periodSeconds = null)
        String anchor           // "ZONE:HH:mm", напр. "UTC:00:00" или "Europe/Moscow:00:00"
) {}