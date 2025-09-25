package com.example.limits.application.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Обёртка результата операций сервисного уровня.
 * <p>
 * Используется контроллерами для единообразного ответа и
 * построения HTTP-статусов/заголовков:
 * <ul>
 *   <li>{@code created == true}  → 201 Created (+ Location по {@code resourceId})</li>
 *   <li>{@code created == false} → 200 OK</li>
 * </ul>
 *
 * @param <T> тип полезной нагрузки (DTO, ответ домена и т.п.)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Результат выполнения операции")
public record OperationResult<T>(

        @Schema(description = "Флаг: ресурс создан (для 201 Created)", example = "true")
        boolean created,

        @Schema(description = "Идентификатор ресурса (используется для Location)", example = "42")
        String resourceId,

        @Schema(description = "Полезная нагрузка ответа")
        T payload
) {

    /**
     * Фабрика результата «создано».
     *
     * @param id      идентификатор созданного ресурса (идёт в Location)
     * @param payload полезная нагрузка
     * @return результат с {@code created=true}
     */
    public static <T> OperationResult<T> created(String id, T payload) {
        return new OperationResult<>(true, id, payload);
        // Контроллер обычно вернёт: 201 + Location:/.../{id}
    }

    /**
     * Фабрика результата «ОК» (идемпотентный повтор или обычный успешный ответ).
     *
     * @param id      идентификатор ресурса (может быть null, если не применимо)
     * @param payload полезная нагрузка
     * @return результат с {@code created=false}
     */
    public static <T> OperationResult<T> ok(String id, T payload) {
        return new OperationResult<>(false, id, payload);
    }
}
