package com.example.limits.domain.factory;

import com.example.limits.web.dto.AssignUserStrategyRequest;
import com.example.limits.domain.entity.Strategy;
import com.example.limits.domain.entity.UserStrategy;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Фабрика для создания и обновления привязок пользователя к стратегиям.
 * Никакой логики репозитория/валидации — только сборка/модификация сущностей.
 */
@Component
public class UserStrategyFactory {

    /**
     * Создать новую привязку пользователя к стратегии.
     *
     * @param userId        идентификатор пользователя
     * @param strategy      стратегия
     * @param isActive      активна ли привязка (если null, считаем true)
     * @param effectiveFrom дата начала действия (UTC), может быть null
     * @param effectiveTo   дата окончания действия (UTC), может быть null
     */
    public UserStrategy newBinding(String userId,
                                   Strategy strategy,
                                   Boolean isActive,
                                   Instant effectiveFrom,
                                   Instant effectiveTo) {
        UserStrategy userStrategy = new UserStrategy();
        userStrategy.setUserId(userId);
        userStrategy.setStrategy(strategy);
        userStrategy.setActive(isActive == null || isActive);
        userStrategy.setEffectiveFrom(effectiveFrom);
        userStrategy.setEffectiveTo(effectiveTo);
        return userStrategy;
    }

    /**
     * strategy должна быть уже загружена и провалидирована выше по стеку.
     */
    public UserStrategy newBinding(String userId,
                                   Strategy strategy,
                                   AssignUserStrategyRequest request) {
        Boolean isActive = request.isActive();
        Instant effectiveFrom = request.effectiveFrom();
        Instant effectiveTo = request.effectiveTo();
        return newBinding(userId, strategy, isActive, effectiveFrom, effectiveTo);
    }

    /**
     * Обновить существующую привязку управляемыми полями.
     * Если параметр равен null — поле не трогаем (оставляем текущее).
     */
    public UserStrategy updateBinding(UserStrategy target,
                                      Boolean isActive,
                                      Instant effectiveFrom,
                                      Instant effectiveTo) {
        if (isActive != null) {
            target.setActive(isActive);
        }
        if (effectiveFrom != null) {
            target.setEffectiveFrom(effectiveFrom);
        }
        if (effectiveTo != null) {
            target.setEffectiveTo(effectiveTo);
        }
        return target;
    }

    /**
     * Деактивировать привязку.
     */
    public UserStrategy deactivate(UserStrategy target) {
        target.setActive(false);
        return target;
    }
}
