package com.example.limits.application.service.handler.strategy.assign.impl;

import com.example.limits.application.service.handler.strategy.assign.AssignContext;
import com.example.limits.application.service.handler.strategy.assign.AssignStep;
import com.example.limits.domain.entity.UserStrategy;
import com.example.limits.domain.repository.UserStrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Если новая привязка должна быть активной "на сейчас",
 * снимает признак активности со всех других активных записей пользователя.
 * Идемпотентность: если уже есть активная запись ровно для той же стратегии — выходим.
 */
@Component
@Order(6)
@RequiredArgsConstructor
public class DeactivatePreviousActiveStep implements AssignStep {

    private final UserStrategyRepository userStrategyRepository;

    @Override
    public void execute(AssignContext context) {
        Boolean normalizedActive = context.getNormalizedActive();
        if (!Boolean.TRUE.equals(normalizedActive)) {
            return;
        }

        UserStrategy existingSameStrategy = context.getExistingForSameStrategy();
        if (existingSameStrategy != null && existingSameStrategy.isActive()) {
            return;
        }

        userStrategyRepository.deactivateAllActiveForUser(context.getUserId());
    }
}
