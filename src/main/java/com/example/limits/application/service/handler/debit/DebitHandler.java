package com.example.limits.application.service.handler.debit;

import com.example.limits.web.dto.TransactionRequest;

public interface DebitHandler {
    DebitOutcome handle(TransactionRequest req);
}
