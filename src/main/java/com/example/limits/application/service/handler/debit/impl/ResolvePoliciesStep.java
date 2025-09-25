package com.example.limits.application.service.handler.debit.impl;

import com.example.limits.application.service.StrategyService;
import com.example.limits.application.service.handler.debit.DebitContext;
import com.example.limits.application.service.handler.debit.DebitStep;
import com.example.limits.web.dto.TransactionRequest;
import com.example.limits.domain.entity.Strategy;
import com.example.limits.domain.policy.contract.AttrContract;
import com.example.limits.domain.policy.normalizer.AttributesNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Шаг 2. Разрешение применимых политик (strategies) и нормализация атрибутов.
 *
 * 1) Получаем контракт атрибутов (разрешённые/обязательные ключи, регистр, алиасы и т.п.)
 *    для данного пользователя на момент операции; при отсутствии — применяем максимально
 *    permissive контракт.
 * 2) Нормализуем входные атрибуты запроса в соответствии с контрактом и проверяем,
 *    что обязательные ключи присутствуют.
 * 3) По нормализованным атрибутам подбираем стратегии, применимые к данному пользователю.
 *    Список найденных стратегий сохраняем в контекст для следующих шагов.
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class ResolvePoliciesStep implements DebitStep {

    private final StrategyService strategyService;
    private final AttributesNormalizer attributesNormalizer;

    @Override
    public boolean supports(DebitContext context) {
        return context != null && !context.isStopped() && context.getRequest() != null;
    }

    @Override
    public void execute(DebitContext context) {
        TransactionRequest request = context.getRequest();

        String userId = request.userId();
        Instant occurredAt = request.occurredAt();

        AttrContract attributeContract = strategyService
                .attributeContractForUser(userId, occurredAt)
                .orElseGet(AttrContract::permissive);

        Map<String, String> normalizedAttributes =
                attributesNormalizer.normalize(request.attributes(), attributeContract);

        List<String> missingRequiredKeys =
                attributesNormalizer.validateRequired(normalizedAttributes, attributeContract);

        if (!missingRequiredKeys.isEmpty()) {
            // Можно завести специализированное исключение ValidationException/BadRequestException,
            // чтобы глобальный хэндлер вернул 400, но из-за нехватки времени решил оставить IllegalArgumentException.
            throw new IllegalArgumentException(
                    "Missing required attributes: " + String.join(", ", missingRequiredKeys)
            );
        }

        List<Strategy> matchedStrategies =
                strategyService.resolveApplicableForUser(userId, normalizedAttributes, occurredAt);

        context.setNormalizedAttributes(normalizedAttributes);
        context.setPolicies(matchedStrategies);
    }
}
