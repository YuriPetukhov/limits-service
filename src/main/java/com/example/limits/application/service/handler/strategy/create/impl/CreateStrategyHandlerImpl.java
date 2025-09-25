package com.example.limits.application.service.handler.strategy.create.impl;

import com.example.limits.web.dto.CreateStrategyRequest;
import com.example.limits.application.service.handler.strategy.create.CreateStrategyContext;
import com.example.limits.application.service.handler.strategy.create.CreateStrategyHandler;
import com.example.limits.application.service.handler.strategy.create.CreateStrategyOutcome;
import com.example.limits.application.service.handler.strategy.create.CreateStrategyStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Конвейер создания стратегии.
 * Порядок шагов задаётся @Order на бинах шагов или явной сборкой списка в конфиге.
 */
@Component
@RequiredArgsConstructor
public class CreateStrategyHandlerImpl implements CreateStrategyHandler {

    private final List<CreateStrategyStep> steps;

    @Override
    public CreateStrategyOutcome handle(CreateStrategyRequest req) {
        CreateStrategyContext ctx = new CreateStrategyContext(req);
        for (CreateStrategyStep step : steps) {
            step.execute(ctx);
            if (ctx.isStopped()) break;
        }
        String resourceId = (ctx.getResourceId() != null) ? ctx.getResourceId() : ctx.fallbackResourceId();
        return new CreateStrategyOutcome(ctx.isCreated(), resourceId, ctx.getResponse());
    }
}