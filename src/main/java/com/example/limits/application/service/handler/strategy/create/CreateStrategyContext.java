package com.example.limits.application.service.handler.strategy.create;

import com.example.limits.web.dto.CreateStrategyRequest;
import com.example.limits.web.dto.StrategyResponse;
import com.example.limits.domain.entity.Strategy;
import lombok.Getter;
import lombok.Setter;

/** Контекст пайплайна создания стратегии. Шаги выставляют created/resourceId/response и могут останавливать конвейер. */
@Getter
public class CreateStrategyContext {
    private final CreateStrategyRequest request;

    private StrategyResponse response;
    private boolean stopped;

    @Setter private boolean created;
    @Setter private String resourceId;

    @Setter private Boolean normalizedEnabled;
    @Setter private Boolean normalizedDefault;

    @Setter private Strategy strategyEntity;

    public CreateStrategyContext(CreateStrategyRequest request) {
        this.request = request;
    }

    public void stop() { this.stopped = true; }

    public void stopWith(StrategyResponse response) {
        this.response = response;
        this.stopped = true;
    }

    public void setResponse(StrategyResponse response) {
        this.response = response;
    }

    /** Фолбэк на случай, если шаги не выставили resourceId. */
    public String fallbackResourceId() {
        // Можно вернуть name:version как стабильный surrogate, но лучше, чтобы шаги проставляли фактический ID.
        return this.request.name() + ":" + this.request.version();
    }
}
