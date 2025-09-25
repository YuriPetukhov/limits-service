package com.example.limits.application.service.handler.strategy.create.impl;

import com.example.limits.application.service.handler.strategy.create.CreateStrategyContext;
import com.example.limits.application.service.handler.strategy.create.CreateStrategyStep;
import com.example.limits.web.dto.StrategyResponse;
import com.example.limits.domain.entity.Strategy;
import com.example.limits.web.mapper.StrategyMapper;
import com.example.limits.domain.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сохраняет стратегию и формирует ответ.
 * Если просили сделать дефолтной — переключает дефолт атомарно (двумя апдейтами).
 */
@Component
@Order(5)
@RequiredArgsConstructor
public class PersistAndMapStep implements CreateStrategyStep {

    private final StrategyRepository strategyRepository;
    private final StrategyMapper strategyMapper;

    @Override
    @Transactional
    public void execute(CreateStrategyContext context) {
        Strategy toSave = context.getStrategyEntity();

        boolean makeDefault = Boolean.TRUE.equals(context.getNormalizedDefault());
        if (makeDefault) {
            // На INSERT не ставим true — сначала сохраняем, затем переключаем флаг, чтобы не нарушить partial UNIQUE
            toSave.setDefault(false);
        }

        Strategy saved = strategyRepository.save(toSave);

        if (makeDefault) {
            strategyRepository.clearOtherDefaults(saved.getId());
            strategyRepository.setDefaultById(saved.getId());
            saved.setDefault(true); // синхронизируем в памяти
        }

        StrategyResponse response = strategyMapper.toResponse(saved);
        context.setCreated(true);
        context.setResourceId(String.valueOf(saved.getId()));
        context.setResponse(response);
        context.stop();
    }
}
