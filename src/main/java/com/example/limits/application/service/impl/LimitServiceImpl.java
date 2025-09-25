package com.example.limits.application.service.impl;

import com.example.limits.application.service.LimitService;
import com.example.limits.application.service.handler.debit.ScopePlan;
import com.example.limits.domain.entity.LimitTxRegistry;
import com.example.limits.web.dto.TransactionRequest;
import com.example.limits.web.dto.TransactionResponse;
import com.example.limits.domain.entity.LimitBucket;
import com.example.limits.domain.factory.LimitBucketFactory;
import com.example.limits.web.mapper.BucketMutationMapper;
import com.example.limits.web.mapper.BucketUsageMapper;
import com.example.limits.web.mapper.RegistryRowMapper;
import com.example.limits.web.mapper.RemainingMapper;
import com.example.limits.web.mapper.TransactionResponseMapper;
import com.example.limits.domain.repository.LimitBucketRepository;
import com.example.limits.domain.repository.LimitTxRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис работы с «ведрами» (бакетами) лимитов и реестром транзакций.
 * Содержит только методы, которые вызываются шагами пайплайна.
 */
@Service
@RequiredArgsConstructor
public class LimitServiceImpl implements LimitService {

    private final LimitBucketRepository limitBucketRepository;
    private final LimitTxRegistryRepository limitTxRegistryRepository;

    private final BucketUsageMapper bucketUsageMapper;
    private final RegistryRowMapper registryRowMapper;
    private final BucketMutationMapper bucketMutationMapper;
    private final LimitBucketFactory limitBucketFactory;
    private final RemainingMapper remainingMapper;
    private final TransactionResponseMapper transactionResponseMapper;

    /**
     * Посчитать уже использованные суммы по заданным scope на нужное окно.
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> loadUsed(String userId,
                                            Instant periodStart,
                                            Collection<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return Map.of();
        }

        Map<String, LimitBucket> bucketsByScope = limitBucketRepository
                .findAllByUserIdAndScopeKeyIn(userId, scopes)
                .stream()
                .collect(Collectors.toMap(LimitBucket::getScopeKey, b -> b));

        Map<String, BigDecimal> usedByScope = new HashMap<>();
        for (String scope : scopes) {
            LimitBucket bucket = bucketsByScope.get(scope);
            BigDecimal used = bucketUsageMapper.usedForPeriod(bucket, periodStart);
            usedByScope.put(scope, used);
        }
        return usedByScope;
    }

    /**
     * Забронировать и залогировать списание: ensure-бакеты → журнал → дебет из бакетов.
     */
    @Override
    @Transactional
    public TransactionResponse reserveAndLog(TransactionRequest request,
                                             java.util.List<ScopePlan> scopePlans,
                                             Map<String, BigDecimal> remainingBeforeByScope) {

        Instant periodStart = scopePlans.get(0).periodStart();
        BigDecimal amount = request.amount();

        for (ScopePlan plan : scopePlans) {
            limitBucketFactory.ensureForWindow(
                    request.userId(),
                    plan.scopeKey(),
                    periodStart,
                    plan.limit(),
                    plan.intervalSeconds(),
                    plan.nextResetAt()
            );
        }

        java.util.List<LimitTxRegistry> rows = scopePlans.stream()
                .map(p -> registryRowMapper.toEntity(
                        request.userId(), request.txId(), amount, periodStart, p.scopeKey()))
                .toList();
        limitTxRegistryRepository.saveAll(rows);

        for (ScopePlan plan : scopePlans) {
            LimitBucket bucket = limitBucketRepository
                    .findByUserIdAndScopeKeyForUpdate(request.userId(), plan.scopeKey())
                    .orElseThrow(() -> new IllegalStateException(
                            "Bucket not found after ensureForWindow for scope=" + plan.scopeKey()));

            LimitBucket updated = bucketMutationMapper.applyDebit(
                    request.userId(), bucket, plan, periodStart, amount);
            limitBucketRepository.save(updated);
        }

        Map<String, BigDecimal> remainingAfter = remainingMapper.after(remainingBeforeByScope, amount);
        return transactionResponseMapper.approved(amount, remainingAfter);
    }
}
