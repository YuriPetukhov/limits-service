package com.example.limits.application.service;

import com.example.limits.web.dto.TransactionResponse;

import java.util.Optional;

public interface TxRegistryService {
    Optional<TransactionResponse> findResponse(String userId, String txId);
}
