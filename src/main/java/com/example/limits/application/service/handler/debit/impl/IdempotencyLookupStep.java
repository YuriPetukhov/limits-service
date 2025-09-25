package com.example.limits.application.service.handler.debit.impl;

import com.example.limits.application.service.TxRegistryService;
import com.example.limits.application.service.handler.debit.DebitContext;
import com.example.limits.application.service.handler.debit.DebitStep;
import com.example.limits.web.dto.TransactionRequest;
import com.example.limits.web.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Шаг 1. Идемпотентность.
 *
 * Если операция с данным (userId, txId) уже проводилась, достаём ранее сформированный
 * {@link TransactionResponse} и немедленно останавливаем конвейер.
 *
 * Это гарантирует, что повторные запросы с тем же txId не приведут к двойному списанию.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class IdempotencyLookupStep implements DebitStep {

    /** Сервис доступа к реестру транзакций / кешу идемпотентных ответов. */
    private final TxRegistryService transactionRegistryService;

    @Override
    public boolean supports(DebitContext context) {
        return context != null && !context.isStopped();
    }

    @Override
    public void execute(DebitContext context) {
        TransactionRequest request = context.getRequest();
        if (request == null) {
            return;
        }

        String userId = request.userId();
        String txId   = request.txId();

        Optional<TransactionResponse> existingResponse =
                transactionRegistryService.findResponse(userId, txId);

        existingResponse.ifPresent(context::stopWith);
    }
}
