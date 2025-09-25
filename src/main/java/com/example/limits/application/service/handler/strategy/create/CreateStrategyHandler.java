package com.example.limits.application.service.handler.strategy.create;

import com.example.limits.web.dto.CreateStrategyRequest;

public interface CreateStrategyHandler {
    CreateStrategyOutcome handle(CreateStrategyRequest req);
}
