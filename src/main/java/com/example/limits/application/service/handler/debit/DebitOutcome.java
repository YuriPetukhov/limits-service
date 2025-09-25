package com.example.limits.application.service.handler.debit;

import com.example.limits.web.dto.TransactionResponse;

/**
 * Итог выполнения конвейера debit.
 * <p>
 * Поля:
 * <ul>
 *   <li>{@code created} — true, если это «новый результат» (а не идемпотентный повтор);</li>
 *   <li>{@code resourceId} — идентификатор созданного/найденного ресурса (обычно txId);</li>
 *   <li>{@code response} — итоговый ответ для API.</li>
 * </ul>
 */
public record DebitOutcome(boolean created, String resourceId, TransactionResponse response) {

    /** Конструктор для «создано». */
    public static DebitOutcome created(String resourceId, TransactionResponse response) {
        return new DebitOutcome(true, resourceId, response);
    }

    /** Конструктор для «идемпотентный повтор / ок». */
    public static DebitOutcome ok(String resourceId, TransactionResponse response) {
        return new DebitOutcome(false, resourceId, response);
    }
}
