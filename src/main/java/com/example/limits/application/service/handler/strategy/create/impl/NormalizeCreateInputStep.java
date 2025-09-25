package com.example.limits.application.service.handler.strategy.create.impl;

import com.example.limits.application.service.handler.strategy.create.CreateStrategyContext;
import com.example.limits.application.service.handler.strategy.create.CreateStrategyStep;
import com.example.limits.web.dto.CreateStrategyRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Нормализует флаги enabled / isDefault, если пришли null.
 */
@Component
@Order(1)
public class NormalizeCreateInputStep implements CreateStrategyStep {

    @Override
    public void execute(CreateStrategyContext context) {
        CreateStrategyRequest request = context.getRequest();

        Boolean normalizedEnabled  = (request.enabled()   == null) ? Boolean.TRUE  : request.enabled();
        Boolean normalizedDefault  = (request.isDefault() == null) ? Boolean.FALSE : request.isDefault();

        context.setNormalizedEnabled(normalizedEnabled);
        context.setNormalizedDefault(normalizedDefault);
    }
}
