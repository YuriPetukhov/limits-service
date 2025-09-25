package com.example.limits.application.service.handler.debit;

import com.example.limits.web.dto.TransactionRequest;
import com.example.limits.web.dto.TransactionResponse;
import com.example.limits.domain.entity.Strategy;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Контекст выполнения конвейера debit.
 * <p>
 * Живёт в рамках одного запроса {@link TransactionRequest} и передаётся по шагам.
 * Шаги читают/пишут необходимые поля и могут останавливать конвейер
 * (например, при идемпотентном повторе или ошибке валидации).
 */
@Getter
public final class DebitContext {

    /** Исходный запрос.*/
    private final TransactionRequest request;

    /** Нормализованный userId. */
    @Setter private String userId;

    /** Итоговый ответ (если сформирован одним из шагов). */
    private TransactionResponse response;

    /** Флаг остановки конвейера (после него оставшиеся шаги не выполняются). */
    private boolean stopped;

    /** Признак «создан новый результат» (а не идемпотентный повтор). */
    @Setter private boolean created;

    /** Идентификатор ресурса для Location/аудита (обычно txId). */
    @Setter private String resourceId;

    /** Стратегии, подобранные под запрос (после матчинга). */
    @Setter private List<Strategy> policies;

    /** Был ли использован fallback (дефолтная политика и т.п.). */
    @Setter private boolean fallbackUsed;

    /** Причины, почему некоторые политики не подошли (опционально, для диагностики). */
    @Setter private List<String> policyMiss;

    /** План списаний по окнам: scope + окно + лимит. */
    @Setter private List<ScopePlan> plans;

    /** Фактическое использование по скоупам в текущем окне. */
    @Setter private Map<String, BigDecimal> usedByScope;

    /** Остатки ДО списания по каждому скоупу. */
    @Setter private Map<String, BigDecimal> remainingBeforeByScope;

    /** Остатки ПОСЛЕ списания по каждому скоупу (для ответа). */
    @Setter private Map<String, BigDecimal> remainingAfterByScope;

    /** Нормализованные атрибуты запроса (после атрибутного контракта/алиасов/кейса). */
    @Setter private Map<String, String> normalizedAttributes;

    public DebitContext(TransactionRequest request) {
        this.request = request;
    }

    /** Мягкая остановка без ответа (не для прода). */
    public void stop() {
        this.stopped = true;
    }

    /** Остановка с установленным ответом.*/
    public void stopWith(TransactionResponse response) {
        this.response = response;
        this.stopped = true;
    }

    /** Явно проставить ответ (без остановки конвейера).*/
    public void setResponse(TransactionResponse response) {
        this.response = response;
    }

    /**
     * Закончить конвейер идемпотентным повтором.
     * <p>Устанавливает {@code created=false}, {@code resourceId=txId}, ответ и останавливает конвейер.
     */
    public void markIdempotent(String txId, TransactionResponse response) {
        this.created = false;
        this.resourceId = txId;
        this.response = response;
        this.stopped = true;
    }

    /**
     * Закончить конвейер «создано».
     * <p>Устанавливает {@code created=true}, {@code resourceId=txId}, ответ и останавливает конвейер.
     */
    public void markCreated(String txId, TransactionResponse response) {
        this.created = true;
        this.resourceId = txId;
        this.response = response;
        this.stopped = true;
    }
}
