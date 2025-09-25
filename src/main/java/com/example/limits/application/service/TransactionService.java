package com.example.limits.application.service;

import com.example.limits.application.result.OperationResult;
import com.example.limits.web.dto.*;

public interface TransactionService {
    OperationResult<TransactionResponse> debit(TransactionRequest req);
    OperationResult<CancelResponse> cancel(CancelRequest req);
    TransactionResponse getById(String userId, String txId);
    CheckLimitResponse check(CheckLimitRequest req);

}
