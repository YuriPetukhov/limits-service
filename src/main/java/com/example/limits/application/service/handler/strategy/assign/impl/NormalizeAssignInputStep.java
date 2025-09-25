package com.example.limits.application.service.handler.strategy.assign.impl;

import com.example.limits.application.service.handler.strategy.assign.AssignContext;
import com.example.limits.application.service.handler.strategy.assign.AssignStep;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/** Нормализует флаги и вычисляет, активна ли привязка на момент now. */
@Component
@Order(1)
@RequiredArgsConstructor
public class NormalizeAssignInputStep implements AssignStep {

    private final Clock clock = Clock.systemUTC();

    @Override
    public void execute(AssignContext context) {
        Boolean requestedActive = context.getRequest().isActive();
        boolean normalizedActive = (requestedActive == null) ? true : requestedActive;

        Instant effectiveFrom = context.getRequest().effectiveFrom();
        Instant effectiveTo   = context.getRequest().effectiveTo();
        Instant now           = Instant.now(clock);

        boolean becomesActiveNow =
                normalizedActive
                        && (effectiveFrom == null || !now.isBefore(effectiveFrom))
                        && (effectiveTo == null   ||  now.isBefore(effectiveTo));

        context.setBecomesActiveNow(becomesActiveNow);
        context.setNormalizedActive(normalizedActive);
        context.setNow(now);
    }
}
