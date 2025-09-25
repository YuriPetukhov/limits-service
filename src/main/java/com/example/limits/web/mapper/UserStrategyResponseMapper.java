package com.example.limits.web.mapper;

import com.example.limits.web.dto.UserStrategyResponse;
import com.example.limits.domain.entity.UserStrategy;
import org.springframework.stereotype.Component;

/** Преобразует привязку пользователя к стратегии в DTO для API. */
@Component
public class UserStrategyResponseMapper {

    public UserStrategyResponse toResponse(UserStrategy entity) {
        return new UserStrategyResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getStrategy().getId(),
                entity.getStrategy().getName(),
                entity.getStrategy().getVersion(),
                entity.isActive(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
