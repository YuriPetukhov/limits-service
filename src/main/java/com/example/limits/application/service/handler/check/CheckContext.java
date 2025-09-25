package com.example.limits.application.service.handler.check;

import com.example.limits.web.dto.CheckLimitRequest;
import com.example.limits.web.dto.CheckLimitResponse;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Контекст конвейера проверки лимита (check).
 * <p>
 * Живёт в рамках одного {@link CheckLimitRequest} и передаётся по шагам пайплайна.
 * Шаги могут писать агрегаты (например, суммарный доступный остаток) и
 * останавливать конвейер, формируя {@link #response}.
 */
@Getter
public final class CheckContext {

    /** Исходный запрос. Не {@code null}. */
    private final CheckLimitRequest request;

    /** Итоговый ответ, если сформирован одним из шагов. */
    private CheckLimitResponse response;

    /** Флаг остановки конвейера (оставшиеся шаги не выполняются). */
    private boolean stopped;

    /** Суммарный доступный остаток по пользователю/скоупам (заполняется шагами). */
    @Setter
    private BigDecimal totalRemaining = BigDecimal.ZERO;

    public CheckContext(CheckLimitRequest request) {
        this.request = request;
    }

    /** Остановка с установленным ответом.*/
    public void stopWith(CheckLimitResponse response) {
        this.response = response;
        this.stopped = true;
    }

    /** Явно проставить ответ (без остановки конвейера).*/
    public void setResponse(CheckLimitResponse response) {
        this.response = response;
    }

    /** Мягкая остановка без ответа*/
    public void stop() {
        this.stopped = true;
    }
}
