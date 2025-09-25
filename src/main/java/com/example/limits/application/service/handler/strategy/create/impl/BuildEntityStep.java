package com.example.limits.application.service.handler.strategy.create.impl;

import com.example.limits.application.service.handler.strategy.create.CreateStrategyContext;
import com.example.limits.application.service.handler.strategy.create.CreateStrategyStep;
import com.example.limits.web.dto.CreateStrategyRequest;
import com.example.limits.domain.entity.Strategy;
import com.example.limits.domain.factory.StrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Собирает Strategy из запроса через фабрику.
 */
@Component
@Order(4)
@RequiredArgsConstructor
public class BuildEntityStep implements CreateStrategyStep {

    private final StrategyFactory strategyFactory;

    @Override
    public void execute(CreateStrategyContext context) {
        CreateStrategyRequest request = context.getRequest();

        Strategy entity = strategyFactory.buildFromRequest(
                request,
                context.getNormalizedEnabled(),
                context.getNormalizedDefault()
        );

        context.setStrategyEntity(entity);
    }
}
