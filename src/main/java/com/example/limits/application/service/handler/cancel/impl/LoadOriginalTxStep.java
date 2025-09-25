package com.example.limits.application.service.handler.cancel.impl;

import com.example.limits.application.service.handler.cancel.CancelContext;
import com.example.limits.application.service.handler.cancel.CancelStep;
import com.example.limits.web.dto.CancelRequest;
import com.example.limits.domain.entity.LimitTxRegistry;
import com.example.limits.domain.exception.TransactionNotFoundException;
import com.example.limits.domain.repository.LimitTxRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Шаг 2. Загружаем исходную дебет-операцию по (userId, originalTxId).
 * Также вытягиваем связанный бакет (LAZY разрешён, т.к. шаги выполняются внутри @Transactional сервиса/хендлера).
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class LoadOriginalTxStep implements CancelStep {

    private final LimitTxRegistryRepository limitTxRegistryRepository;

    @Override
    public void execute(CancelContext context) {
        CancelRequest request = context.getRequest();

        LimitTxRegistry originalTx = limitTxRegistryRepository
                .findByUserIdAndTxId(request.userId(), request.originalTxId())
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Original tx not found: userId=" + request.userId() + ", txId=" + request.originalTxId()));

        context.setOriginalTx(originalTx);
        context.setBucket(originalTx.getBucket());
    }
}
