package com.example.limits.application.service.impl;

import com.example.limits.application.service.handler.strategy.create.CreateStrategyHandler;
import com.example.limits.application.service.handler.strategy.create.CreateStrategyOutcome;
import com.example.limits.domain.policy.matcher.impl.AttrContractMerger;
import com.example.limits.domain.policy.matcher.impl.StrategySpecParser;
import com.example.limits.web.dto.CreateStrategyRequest;
import com.example.limits.web.dto.StrategyResponse;
import com.example.limits.domain.entity.Strategy;
import com.example.limits.domain.exception.StrategyNotFoundException;
import com.example.limits.domain.factory.StrategyFactory;
import com.example.limits.web.mapper.ScopeMapper;
import com.example.limits.web.mapper.StrategyLimitsMapper;
import com.example.limits.web.mapper.StrategyMapper;
import com.example.limits.domain.repository.StrategyRepository;
import com.example.limits.domain.repository.UserStrategyRepository;
import com.example.limits.application.result.OperationResult;
import com.example.limits.application.service.StrategyService;
import com.example.limits.domain.policy.contract.AttrContract;
import com.example.limits.domain.policy.matcher.PolicyMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис справочника стратегий/политик.
 */
@Service
@RequiredArgsConstructor
public class StrategyServiceImpl implements StrategyService {

    private final StrategyRepository strategyRepository;
    private final UserStrategyRepository userStrategyRepository;
    private final PolicyMatcher policyMatcher;
    private final StrategySpecParser strategySpecParser;
    private final AttrContractMerger attrContractMerger;

    private final CreateStrategyHandler createStrategyHandler;
    private final StrategyMapper strategyMapper;

    private final ScopeMapper scopeMapper;

    @Value("${limits.default.daily:10000.00}")
    private BigDecimal defaultDailyLimit;

    @Value("${limits.default.strategy.name:GLOBAL_DAY_DEFAULT}")
    private String defaultName;

    @Value("${limits.default.strategy.version:1}")
    private int defaultVersion;

    @Override
    @Transactional(readOnly = true)
    public List<Strategy> resolveApplicableForUser(String userId,
                                                   Map<String, String> attributes,
                                                   Instant at) {
        List<Strategy> userStrategies = userStrategyRepository.findActiveStrategies(userId, at);
        List<Strategy> matchedUser = policyMatcher.match(userStrategies, attributes);
        if (!matchedUser.isEmpty()) return matchedUser;

        List<Strategy> defaults = strategyRepository.findAllByEnabledTrueAndIsDefaultTrue();
        return policyMatcher.match(defaults, attributes);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Strategy> resolveBestDefault(String userId,
                                                 Map<String, String> attributes,
                                                 Instant at) {
        List<Strategy> defaults = strategyRepository.findAllByIsDefaultTrueAndEnabledTrue();
        List<Strategy> matched = policyMatcher.match(defaults, attributes);
        return matched.stream()
                .sorted(Comparator.comparing(Strategy::getVersion,
                        Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
                .findFirst();
    }

    @Override
    public String resolveScope(Strategy strategy, String userId, Map<String, String> attributes) {
        return scopeMapper.resolveScope(strategy, userId, attributes);
    }

    @Override
    @Transactional
    public OperationResult<StrategyResponse> create(CreateStrategyRequest request) {
        CreateStrategyOutcome outcome = createStrategyHandler.handle(request);
        return outcome.created()
                ? OperationResult.created(outcome.resourceId(), outcome.response())
                : OperationResult.ok(outcome.resourceId(), outcome.response());
    }

    @Override
    @Transactional(readOnly = true)
    public StrategyResponse getById(Long id) {
        Strategy entity = strategyRepository.findById(id)
                .orElseThrow(() -> new StrategyNotFoundException("Strategy id=" + id + " not found"));
        return strategyMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StrategyResponse> list(Boolean enabled, Boolean isDefault) {
        List<Strategy> items;
        if (enabled == null && isDefault == null) {
            items = strategyRepository.findAll();
        } else if (enabled != null && isDefault != null) {
            items = strategyRepository.findAllByEnabledAndIsDefault(enabled, isDefault);
        } else if (enabled != null) {
            items = strategyRepository.findAllByEnabled(enabled);
        } else {
            items = strategyRepository.findAllByIsDefault(isDefault);
        }
        return items.stream().map(strategyMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public StrategyResponse deactivate(Long id) {
        Strategy entity = strategyRepository.findById(id)
                .orElseThrow(() -> new StrategyNotFoundException("Strategy id=" + id + " not found"));
        if (!entity.isEnabled()) {
            return strategyMapper.toResponse(entity);
        }
        entity.setEnabled(false);
        Strategy saved = strategyRepository.save(entity);
        return strategyMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public StrategyResponse setDefault(Long id) {
        Strategy target = strategyRepository.findById(id)
                .orElseThrow(() -> new StrategyNotFoundException("Strategy id=" + id + " not found"));

        if (target.isDefault()) {
            return strategyMapper.toResponse(target);
        }

        strategyRepository.clearOtherDefaults(id);
        strategyRepository.setDefaultById(id);
        target.setDefault(true);
        return strategyMapper.toResponse(target);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AttrContract> attributeContractForUser(String userId, Instant at) {
        Instant timestamp = (at != null) ? at : Instant.now();

        List<Strategy> activeForUser = userStrategyRepository.findActiveStrategies(userId, timestamp);

        if (activeForUser == null || activeForUser.isEmpty()) {
            activeForUser = strategyRepository.findAllByIsDefaultTrueAndEnabledTrue();
            if (activeForUser == null) activeForUser = List.of();
        }

        List<StrategySpecParser.ValidationSpec> specs = activeForUser.stream()
                .map(strategySpecParser::parseValidation)
                .filter(v -> !v.required().isEmpty() || v.hasAllowed())
                .toList();
        return attrContractMerger.merge(specs);
    }
}
