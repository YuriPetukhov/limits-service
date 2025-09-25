package com.example.limits.application.service.handler.cancel.impl;

import com.example.limits.application.service.handler.cancel.CancelContext;
import com.example.limits.application.service.handler.cancel.CancelStep;
import com.example.limits.web.dto.CancelRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Шаг 1. Базовая валидация запроса на отмену.
 * Требуем:
 *  - userId не пуст,
 *  - originalTxId не пуст (это ИД оригинальной дебет-операции, которую отменяем).
 * Опционально: txId — ИД текущего запроса отмены (если вы его используете в логах/метриках).
 */
@Component
@Order(1)
public class ValidateRequestStep implements CancelStep {

    @Override
    public void execute(CancelContext context) {
        CancelRequest request = context.getRequest();

        if (request == null) {
            throw new IllegalArgumentException("CancelRequest is required");
        }
        if (request.userId() == null || request.userId().isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.originalTxId() == null || request.originalTxId().isBlank()) {
            throw new IllegalArgumentException("originalTxId (id of original debit) is required");
        }

        context.setResourceId(request.originalTxId());
    }
}
