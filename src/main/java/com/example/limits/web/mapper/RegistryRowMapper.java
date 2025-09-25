package com.example.limits.web.mapper;

import com.example.limits.domain.entity.LimitTxRegistry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/** Строит строки реестра транзакций (limit_tx_registry). */
@Component
public class RegistryRowMapper {

    public LimitTxRegistry toEntity(String userId,
                                    String txId,
                                    BigDecimal amount,
                                    Instant periodStart,
                                    String scopeKey) {
        LimitTxRegistry row = new LimitTxRegistry();
        row.setUserId(userId);
        row.setTxId(txId);
        row.setAmount(amount);
        row.setPeriodStart(periodStart);
        row.setScopeKey(scopeKey);
        return row;
    }
}
