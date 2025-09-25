package com.example.limits.application.service;

import com.example.limits.application.service.handler.debit.ScopePlan;
import com.example.limits.web.dto.TransactionRequest;
import com.example.limits.web.dto.TransactionResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface LimitService {

    Map<String, BigDecimal> loadUsed(String userId, Instant periodStart, Collection<String> scopes);

    TransactionResponse reserveAndLog(
            TransactionRequest req,
            List<ScopePlan> plans,
            Map<String, BigDecimal> remainingBeforeByScope);

}
