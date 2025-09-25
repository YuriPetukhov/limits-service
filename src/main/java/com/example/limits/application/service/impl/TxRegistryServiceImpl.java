package com.example.limits.application.service.impl;

import com.example.limits.application.service.TxRegistryService;
import com.example.limits.web.dto.TransactionResponse;
import com.example.limits.web.mapper.TransactionResponseMapper;
import com.example.limits.domain.repository.LimitTxRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Обслуживание идемпотентности по реестру транзакций.
 */
@Service
@RequiredArgsConstructor
public class TxRegistryServiceImpl implements TxRegistryService {

    private final LimitTxRegistryRepository limitTxRegistryRepository;
    private final TransactionResponseMapper transactionResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionResponse> findResponse(String userId, String txId) {
        return limitTxRegistryRepository.findFirstAmountByUserAndTx(userId, txId)
                .map(transactionResponseMapper::idempotentReplay);
    }
}
