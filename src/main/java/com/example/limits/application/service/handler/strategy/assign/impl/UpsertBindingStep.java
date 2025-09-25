package com.example.limits.application.service.handler.strategy.assign.impl;

import com.example.limits.application.service.handler.strategy.assign.AssignContext;
import com.example.limits.application.service.handler.strategy.assign.AssignStep;
import com.example.limits.web.dto.UserStrategyResponse;
import com.example.limits.domain.entity.UserStrategy;
import com.example.limits.domain.factory.UserStrategyFactory;
import com.example.limits.web.mapper.UserStrategyResponseMapper;
import com.example.limits.domain.repository.UserStrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Создаёт новую или обновляет существующую запись user_strategy.
 * Завершает конвейер, устанавливая response/created/resourceId.
 */
@Component
@Order(7)
@RequiredArgsConstructor
public class UpsertBindingStep implements AssignStep {

    private final UserStrategyRepository userStrategyRepository;
    private final UserStrategyResponseMapper userStrategyResponseMapper;
    private final UserStrategyFactory userStrategyFactory;

    @Override
    public void execute(AssignContext context) {
        UserStrategy target = context.getExistingForSameStrategy();
        boolean created;

        if (target != null) {
            target = userStrategyFactory.updateBinding(
                    target,
                    Boolean.TRUE.equals(context.getNormalizedActive()),
                    context.getRequest().effectiveFrom(),
                    context.getRequest().effectiveTo()
            );
            created = false;
        } else {
            target = userStrategyFactory.newBinding(
                    context.getUserId(),
                    context.getStrategy(),
                    Boolean.TRUE.equals(context.getNormalizedActive()),
                    context.getRequest().effectiveFrom(),
                    context.getRequest().effectiveTo()
            );
            created = true;
        }

        UserStrategy saved = userStrategyRepository.save(target);
        UserStrategyResponse response = userStrategyResponseMapper.toResponse(saved);

        context.setCreated(created);
        context.setResourceId(String.valueOf(saved.getId()));
        context.setResponse(response);
        context.stop();
    }
}
