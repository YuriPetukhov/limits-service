package com.example.limits.application.service.handler.check.impl;

import com.example.limits.web.dto.CheckLimitRequest;
import com.example.limits.web.dto.CheckLimitResponse;
import com.example.limits.domain.entity.LimitBucket;
import com.example.limits.web.mapper.CheckLimitResponseMapper;
import com.example.limits.domain.repository.LimitBucketRepository;
import com.example.limits.application.service.handler.check.CheckContext;
import com.example.limits.application.service.handler.check.CheckStep;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Шаг 2. Упрощенный расчёт доступного остатка:
 * суммируем remaining по всем бакетам пользователя и сравниваем с amount.
 *
 * ПРИМЕЧАНИЕ:
 * В будущем можно:
 *  - фильтровать только бакеты активных окон (today/this month и т.п.);
 *  - учитывать scope/атрибуты (категории и пр.);
 *  - агрегировать по совпадающим планам, а не «по всем».
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class ComputeAvailableStep implements CheckStep {

    private final LimitBucketRepository limitBucketRepository;
    private final CheckLimitResponseMapper checkLimitResponseMapper;

    @Override
    public void execute(CheckContext context) {
        CheckLimitRequest request = context.getRequest();

        List<LimitBucket> userBuckets = limitBucketRepository.findAllByUserId(request.userId());

        BigDecimal totalRemaining = userBuckets.stream()
                .map(LimitBucket::getRemaining)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        context.setTotalRemaining(totalRemaining);

        boolean sufficient = totalRemaining.compareTo(request.amount()) >= 0;

        CheckLimitResponse response = sufficient
                ? checkLimitResponseMapper.ok(totalRemaining, request.amount())
                : checkLimitResponseMapper.insufficient(totalRemaining);

        context.setResponse(response);
        context.stopWith(response);
    }
}
