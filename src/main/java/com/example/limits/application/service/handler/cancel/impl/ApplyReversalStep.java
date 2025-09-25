package com.example.limits.application.service.handler.cancel.impl;

import com.example.limits.application.service.handler.cancel.CancelContext;
import com.example.limits.application.service.handler.cancel.CancelStep;
import com.example.limits.web.dto.CancelResponse;
import com.example.limits.web.dto.CancelRequest;
import com.example.limits.domain.entity.LimitBucket;
import com.example.limits.domain.entity.LimitTxRegistry;
import com.example.limits.web.mapper.CancelResponseMapper;
import com.example.limits.domain.repository.LimitBucketRepository;
import com.example.limits.domain.repository.LimitTxRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Шаг 4. Возврат лимита и удаление строки реестра.
 * Стратегия простая: add(amount) в bucket.remaining и delete(originalTx).
 * Если нужна трассировка отмен — можно вместо delete вставлять "reverse" запись.
 */
@Component
@Order(4)
@RequiredArgsConstructor
public class ApplyReversalStep implements CancelStep {

    private final LimitBucketRepository limitBucketRepository;
    private final LimitTxRegistryRepository limitTxRegistryRepository;
    private final CancelResponseMapper cancelResponseMapper;

    @Override
    public void execute(CancelContext context) {
        LimitTxRegistry originalTx = context.getOriginalTx();
        LimitBucket bucket = context.getBucket();

        BigDecimal newRemaining = bucket.getRemaining().add(originalTx.getAmount());
        bucket.setRemaining(newRemaining);
        limitBucketRepository.save(bucket);

        limitTxRegistryRepository.delete(originalTx);

        CancelRequest request = context.getRequest();
        CancelResponse response = cancelResponseMapper.fromBucket(
                bucket,
                /*reverted*/ true,
                "Reversal applied for originalTxId=" + request.originalTxId()
        );

        context.setCreated(true);
        context.setResponse(response);
        context.stop();
    }
}
