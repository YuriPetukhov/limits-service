package com.example.limits.application.service.handler.strategy.assign;

import com.example.limits.web.dto.AssignUserStrategyRequest;

public interface AssignHandler {
    AssignOutcome handle(String userId, AssignUserStrategyRequest req);
}
