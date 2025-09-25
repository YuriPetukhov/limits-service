package com.example.limits.application.service.handler.check.impl;

import com.example.limits.application.service.handler.check.CheckContext;
import com.example.limits.application.service.handler.check.CheckStep;
import com.example.limits.web.dto.CheckLimitRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Шаг 1. Базовая валидация входного запроса на проверку лимита.
 * Проверяем, что userId непустой, а amount > 0.
 */
@Component
@Order(1)
public class ValidateCheckRequestStep implements CheckStep {

    @Override
    public void execute(CheckContext context) {
        CheckLimitRequest request = context.getRequest();

        if (request == null) {
            throw new IllegalArgumentException("CheckLimitRequest is required");
        }
        if (request.userId() == null || request.userId().isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
    }
}
