package com.example.limits.application.service.handler.strategy.assign.impl;

import com.example.limits.application.service.handler.strategy.assign.AssignContext;
import com.example.limits.application.service.handler.strategy.assign.AssignStep;
import com.example.limits.domain.entity.Strategy;
import com.example.limits.domain.exception.StrategyNotFoundException;
import com.example.limits.domain.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Загружает стратегию по strategyId и валидирует, что она включена (enabled=true). */
@Component
@Order(3)
@RequiredArgsConstructor
public class LoadStrategyStep implements AssignStep {

    private final StrategyRepository strategyRepository;

    @Override
    public void execute(AssignContext context) {
        Long strategyId = context.getRequest().strategyId();

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new StrategyNotFoundException("Strategy id=" + strategyId + " not found"));

        if (!strategy.isEnabled()) {
            throw new IllegalStateException("Strategy id=" + strategy.getId() + " is disabled");
        }

        context.setStrategy(strategy);
    }
}
