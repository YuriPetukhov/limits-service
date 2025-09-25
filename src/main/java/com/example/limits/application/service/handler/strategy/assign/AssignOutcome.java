package com.example.limits.application.service.handler.strategy.assign;

import com.example.limits.web.dto.UserStrategyResponse;

/** Результат конвейера назначения стратегии пользователю. */
public record AssignOutcome(boolean created, String resourceId, UserStrategyResponse response) { }
