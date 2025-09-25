package com.example.limits.web.mapper;

import com.example.limits.web.dto.enums.DecisionStatus;
import com.example.limits.web.dto.TransactionResponse;
import com.example.limits.domain.entity.LimitBucket;
import com.example.limits.domain.entity.LimitTxRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TransactionResponseMapper {

    public TransactionResponse idempotentReplay(BigDecimal amount) {
        return new TransactionResponse(
                DecisionStatus.APPROVED,
                "OK (idempotent replay)",
                amount,
                Collections.emptyMap()
        );
    }

    // ответ из группы строк реестра
    public TransactionResponse fromRegistryGroup(List<LimitTxRegistry> rows) {
        if (rows == null || rows.isEmpty()) return null;

        BigDecimal debited = rows.stream()
                .map(LimitTxRegistry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // снапшота нет — оставляем пусто
        Map<String, BigDecimal> remainingByScope = Collections.emptyMap();

        return new TransactionResponse(
                DecisionStatus.APPROVED,
                "OK (idempotent replay)",
                debited,
                remainingByScope
        );
    }

    //успех
    public TransactionResponse approved(BigDecimal debited, Map<String, BigDecimal> remainingAfterByScope) {
        return new TransactionResponse(
                DecisionStatus.APPROVED,
                "OK",
                debited,
                remainingAfterByScope == null ? Collections.emptyMap() : remainingAfterByScope
        );
    }

    // отказ
    public TransactionResponse declined(String reason, BigDecimal attempted) {
        return new TransactionResponse(
                DecisionStatus.DECLINED,
                reason,
                attempted == null ? BigDecimal.ZERO : attempted,
                Collections.emptyMap()
        );
    }

    /** Ответ для успешно проведённого дебета: статус APPROVED, причина OK, сумма = списанная, остаток — текущий в ведре. */
    public TransactionResponse fromRegistry(LimitTxRegistry reg, LimitBucket bucket) {
        return new TransactionResponse(
                DecisionStatus.APPROVED,
                "OK",
                reg.getAmount(),
                Map.of(bucket.getScopeKey(), bucket.getRemaining())
        );
    }
}

