package com.example.limits.application.service.impl;

import com.example.limits.application.service.handler.cancel.CancelHandler;
import com.example.limits.application.service.handler.cancel.CancelOutcome;
import com.example.limits.application.service.handler.debit.DebitHandler;
import com.example.limits.application.service.handler.debit.DebitOutcome;
import com.example.limits.web.dto.CancelRequest;
import com.example.limits.web.dto.CancelResponse;
import com.example.limits.web.dto.CheckLimitRequest;
import com.example.limits.web.dto.CheckLimitResponse;
import com.example.limits.web.dto.TransactionRequest;
import com.example.limits.web.dto.TransactionResponse;
import com.example.limits.domain.entity.LimitBucket;
import com.example.limits.domain.entity.LimitTxRegistry;
import com.example.limits.domain.exception.TransactionNotFoundException;
import com.example.limits.web.mapper.TransactionResponseMapper;
import com.example.limits.domain.repository.LimitBucketRepository;
import com.example.limits.domain.repository.LimitTxRegistryRepository;
import com.example.limits.application.result.OperationResult;
import com.example.limits.application.service.TransactionService;
import com.example.limits.application.service.handler.check.CheckHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Фасад транзакций: дебет, отмена, получение результата, «check».
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final DebitHandler debitHandler;
    private final CancelHandler cancelHandler;
    private final CheckHandler checkHandler;
    private final LimitTxRegistryRepository limitTxRegistryRepository;
    private final LimitBucketRepository limitBucketRepository;
    private final TransactionResponseMapper transactionResponseMapper;

    @Override
    public OperationResult<TransactionResponse> debit(TransactionRequest request) {
        DebitOutcome outcome = debitHandler.handle(request);
        return outcome.created()
                ? OperationResult.created(outcome.resourceId(), outcome.response())
                : OperationResult.ok(outcome.resourceId(), outcome.response());
    }

    @Override
    @Transactional
    public OperationResult<CancelResponse> cancel(CancelRequest request) {
        CancelOutcome outcome = cancelHandler.handle(request);
        return outcome.created()
                ? OperationResult.created(outcome.resourceId(), outcome.response())
                : OperationResult.ok(outcome.resourceId(), outcome.response());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getById(String userId, String txId) {
        LimitTxRegistry registryRow = limitTxRegistryRepository.findByUserIdAndTxId(userId, txId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found: userId=" + userId + ", txId=" + txId));

        LimitBucket bucket = registryRow.getBucket();
        if (bucket == null) {
            bucket = limitBucketRepository.findByUserIdAndScopeKey(registryRow.getUserId(), registryRow.getScopeKey())
                    .orElseThrow(() -> new IllegalStateException("Bucket not found for scope " + registryRow.getScopeKey()));
        }
        return transactionResponseMapper.fromRegistry(registryRow, bucket);
    }

    @Override
    @Transactional(readOnly = true)
    public CheckLimitResponse check(CheckLimitRequest request) {
        return checkHandler.handle(request);
    }
}
