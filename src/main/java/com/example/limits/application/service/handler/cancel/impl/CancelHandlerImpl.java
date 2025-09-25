package com.example.limits.application.service.handler.cancel.impl;

import com.example.limits.web.dto.CancelRequest;
import com.example.limits.application.service.handler.cancel.CancelContext;
import com.example.limits.application.service.handler.cancel.CancelHandler;
import com.example.limits.application.service.handler.cancel.CancelOutcome;
import com.example.limits.application.service.handler.cancel.CancelStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Конвейер (pipeline) отмены транзакции.
 * Последовательно выполняет шаги, каждый шаг может:
 *  - пропуститься (supports(ctx) == false);
 *  - остановить конвейер (ctx.stop()/ctx.stopWith(...)).
 *
 * На выходе формирует CancelOutcome с флагом created, resourceId и DTO-ответом.
 */
@Component
@RequiredArgsConstructor
public class CancelHandlerImpl implements CancelHandler {

    private final List<CancelStep> steps;

    @Override
    public CancelOutcome handle(CancelRequest request) {
        CancelContext context = new CancelContext(request);

        for (CancelStep step : steps) {
            if (context.isStopped()) break;
            if (!step.supports(context)) continue;
            step.execute(context);
        }

        String resourceId = (context.getResourceId() != null)
                ? context.getResourceId()
                : request.txId();

        return new CancelOutcome(context.isCreated(), resourceId, context.getResponse());
    }
}
