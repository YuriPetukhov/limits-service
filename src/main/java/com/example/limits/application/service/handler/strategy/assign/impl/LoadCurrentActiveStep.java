package com.example.limits.application.service.handler.strategy.assign.impl;

import com.example.limits.application.service.handler.strategy.assign.AssignContext;
import com.example.limits.application.service.handler.strategy.assign.AssignStep;
import com.example.limits.domain.entity.UserStrategy;
import com.example.limits.domain.repository.UserStrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Загружает текущую активную привязку пользователя (если есть).
 * Ничего не изменяет; кладёт найденную запись в контекст.
 */
@Component
@Order(5)
@RequiredArgsConstructor
public class LoadCurrentActiveStep implements AssignStep {

    private final UserStrategyRepository userStrategyRepository;
    private final Clock clock = Clock.systemUTC();

    @Override
    public void execute(AssignContext context) {
        Instant now = (context.getNow() != null) ? context.getNow() : Instant.now(clock);
        Optional<UserStrategy> activeBinding =
                userStrategyRepository.findActiveByUserId(context.getUserId(), now);

        activeBinding.ifPresent(context::setCurrentlyActiveBinding);
    }
}
