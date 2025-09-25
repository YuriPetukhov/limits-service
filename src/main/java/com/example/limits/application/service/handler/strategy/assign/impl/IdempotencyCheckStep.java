package com.example.limits.application.service.handler.strategy.assign.impl;

import com.example.limits.application.service.handler.strategy.assign.AssignContext;
import com.example.limits.application.service.handler.strategy.assign.AssignStep;
import com.example.limits.web.dto.UserStrategyResponse;
import com.example.limits.domain.entity.UserStrategy;
import com.example.limits.web.mapper.UserStrategyResponseMapper;
import com.example.limits.domain.repository.UserStrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Проверяет, есть ли уже привязка user+strategy.
 * Если все управляемые поля совпадают — идемпотентный повтор (останавливаем конвейер).
 * Иначе — помечаем найденную запись для апдейта на последующем шаге.
 */
@Component
@Order(4)
@RequiredArgsConstructor
public class IdempotencyCheckStep implements AssignStep {

    private final UserStrategyRepository userStrategyRepository;
    private final UserStrategyResponseMapper userStrategyResponseMapper;

    @Override
    public void execute(AssignContext context) {
        String userId   = context.getUserId();
        Long strategyId = context.getStrategy().getId();

        Optional<UserStrategy> existingBindingOpt =
                userStrategyRepository.findByUserIdAndStrategyId(userId, strategyId);

        if (existingBindingOpt.isEmpty()) {
            return;
        }

        UserStrategy existingBinding = existingBindingOpt.get();

        boolean sameActive = existingBinding.isActive() == Boolean.TRUE.equals(context.getNormalizedActive());
        boolean sameFrom   = Objects.equals(existingBinding.getEffectiveFrom(), context.getRequest().effectiveFrom());
        boolean sameTo     = Objects.equals(existingBinding.getEffectiveTo(),   context.getRequest().effectiveTo());

        if (sameActive && sameFrom && sameTo) {
            UserStrategyResponse response = userStrategyResponseMapper.toResponse(existingBinding);
            context.setCreated(false);
            context.setResourceId(existingBinding.getId().toString());
            context.stopWith(response);
        } else {
            context.setExistingForSameStrategy(existingBinding);
        }
    }
}
