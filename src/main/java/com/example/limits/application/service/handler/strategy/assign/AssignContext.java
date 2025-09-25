package com.example.limits.application.service.handler.strategy.assign;

import com.example.limits.web.dto.AssignUserStrategyRequest;
import com.example.limits.web.dto.UserStrategyResponse;
import com.example.limits.domain.entity.Strategy;
import com.example.limits.domain.entity.UserStrategy;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Контекст конвейера назначения стратегии. Шаги выставляют created/resourceId/response и могут останавливать пайплайн. */
@Getter
public class AssignContext {
    private final String userId;
    private final AssignUserStrategyRequest request;

    private UserStrategyResponse response;
    private boolean stopped;

    @Getter
    @Setter private boolean created;
    @Setter private String resourceId;

    @Setter private Boolean normalizedActive;
    @Setter private Instant now;
    @Setter private boolean becomesActiveNow;
    @Setter private Strategy strategy;
    @Setter private UserStrategy existingForSameStrategy;
    @Setter private UserStrategy currentlyActiveBinding;

    public AssignContext(String userId, AssignUserStrategyRequest request) {
        this.userId = userId;
        this.request = request;
    }

    public void stop() { this.stopped = true; }

    public void stopWith(UserStrategyResponse resp) {
        this.response = resp;
        this.stopped = true;
    }

    public void setResponse(UserStrategyResponse resp) { this.response = resp; }

    /** На случай, если шаги не проставили resourceId — возвращаем что-то стабильное. */
    public String fallbackResourceId() {
        return this.userId;
    }

}
