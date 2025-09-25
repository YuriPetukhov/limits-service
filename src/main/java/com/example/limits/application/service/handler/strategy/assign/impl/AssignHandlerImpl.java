package com.example.limits.application.service.handler.strategy.assign.impl;

import com.example.limits.web.dto.AssignUserStrategyRequest;
import com.example.limits.application.service.handler.strategy.assign.AssignHandler;
import com.example.limits.application.service.handler.strategy.assign.AssignOutcome;
import com.example.limits.application.service.handler.strategy.assign.AssignStep;
import com.example.limits.application.service.handler.strategy.assign.AssignContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssignHandlerImpl implements AssignHandler {

    private final List<AssignStep> steps;

    public AssignHandlerImpl(List<AssignStep> steps) {
        this.steps = steps;
    }

    @Override
    public AssignOutcome handle(String userId, AssignUserStrategyRequest req) {
        AssignContext ctx = new AssignContext(userId, req);
        for (AssignStep step : steps) {
            step.execute(ctx);
            if (ctx.isStopped()) break;
        }
        String resourceId = (ctx.getResourceId() != null) ? ctx.getResourceId() : ctx.fallbackResourceId();
        return new AssignOutcome(ctx.isCreated(), resourceId, ctx.getResponse());
    }
}
