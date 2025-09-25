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
@Setter
public class CancelContext {

    private final CancelRequest request;

    private boolean stopped;
    private String resourceId;
    private boolean created;
    private CancelResponse response;

    private LimitTxRegistry originalTx;
    private LimitBucket bucket;

    public CancelContext(CancelRequest request) {
        this.request = request;
    }

    public void stop() { this.stopped = true; }

    public void stopWith(CancelResponse response) {
        this.response = response;
        this.stopped = true;
    }
}
