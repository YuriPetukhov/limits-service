package com.example.limits.application.service.handler.strategy.assign.impl;

import com.example.limits.application.service.handler.strategy.assign.AssignContext;
import com.example.limits.application.service.handler.strategy.assign.AssignStep;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Проверяет, что effectiveFrom < effectiveTo (если обе даты заданы). */
@Component
@Order(2)
public class ValidateDatesStep implements AssignStep {

    @Override
    public void execute(AssignContext context) {
        Instant effectiveFrom = context.getRequest().effectiveFrom();
        Instant effectiveTo   = context.getRequest().effectiveTo();

        if (effectiveFrom != null && effectiveTo != null && !effectiveFrom.isBefore(effectiveTo)) {
            throw new IllegalArgumentException("effectiveFrom must be strictly before effectiveTo");
        }
    }
}
