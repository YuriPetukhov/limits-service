package com.example.limits.application.service.handler.cancel;

import com.example.limits.web.dto.CancelRequest;
import com.example.limits.web.dto.CancelResponse;
import com.example.limits.domain.entity.LimitBucket;
import com.example.limits.domain.entity.LimitTxRegistry;
import lombok.Getter;
import lombok.Setter;
/**
 * Контекст конвейера отмены транзакции (reversal).
 * Передаётся между шагами и накапливает промежуточные результаты.
 */
@Getter
public class CancelContext {
    private final CancelRequest request;

    @Setter private boolean stopped;
    @Setter private boolean created;
    @Setter private String resourceId;

    @Setter private LimitTxRegistry originalTx;
    @Setter private LimitBucket bucket;

    @Setter private CancelResponse response;
    public CancelContext(CancelRequest request) { this.request = request; }

    public void stop() { this.stopped = true; }
    public void stopWith(CancelResponse resp) { this.response = resp; this.stopped = true; }
}
