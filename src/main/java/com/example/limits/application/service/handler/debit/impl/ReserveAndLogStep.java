package com.example.limits.application.service.handler.debit.impl;

import com.example.limits.application.service.handler.debit.ScopePlan;
import com.example.limits.web.dto.TransactionRequest;
import com.example.limits.web.dto.TransactionResponse;
import com.example.limits.application.service.LimitService;
import com.example.limits.application.service.handler.debit.DebitStep;
import com.example.limits.application.service.handler.debit.DebitContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Шаг 7. Резервирование суммы и запись в журнал.
 *
 * Предпосылки:
 *  - в контексте есть планы по скоупам (ScopePlan),
 *  - рассчитаны остатки ДО списания (remainingBeforeByScope),
 *  - лимиты уже проверены на достаточность предыдущим шагом.
 *
 * Действия:
 *  - ensure/создание и «перекат» бакетов под окно,
 *  - запись строк журнала транзакций,
 *  - уменьшение остатков по бакетам под блокировкой,
 *  - сбор TransactionResponse и остановка конвейера.
 */
@Component
@Order(7)
@RequiredArgsConstructor
public class ReserveAndLogStep implements DebitStep {

    private final LimitService limitService;

    @Override
    public boolean supports(DebitContext context) {
        return context != null
                && !context.isStopped()
                && context.getPlans() != null
                && !context.getPlans().isEmpty()
                && context.getRemainingBeforeByScope() != null;
    }

    @Override
    @Transactional
    public void execute(DebitContext context) {
        TransactionRequest request = context.getRequest();
        List<ScopePlan> scopePlans = context.getPlans();
        Map<String, java.math.BigDecimal> remainingBeforeByScope = context.getRemainingBeforeByScope();

        TransactionResponse response = limitService.reserveAndLog(
                request,
                scopePlans,
                remainingBeforeByScope
        );

        context.stopWith(response);
    }
}
