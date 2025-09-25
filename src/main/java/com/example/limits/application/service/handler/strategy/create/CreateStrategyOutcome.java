package com.example.limits.application.service.handler.strategy.create;

import com.example.limits.web.dto.StrategyResponse;

/** Результат конвейера создания стратегии. */
public record CreateStrategyOutcome(boolean created, String resourceId, StrategyResponse response) { }
