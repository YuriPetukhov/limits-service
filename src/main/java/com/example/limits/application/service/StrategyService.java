package com.example.limits.application.service;

import com.example.limits.web.dto.CreateStrategyRequest;
import com.example.limits.web.dto.StrategyResponse;
import com.example.limits.domain.entity.Strategy;
import com.example.limits.application.result.OperationResult;
import com.example.limits.domain.policy.contract.AttrContract;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StrategyService {

    List<Strategy> resolveApplicableForUser(String userId, Map<String,String> attributes, Instant at);

    Optional<AttrContract> attributeContractForUser(String userId, Instant at);

    Optional<Strategy> resolveBestDefault(String userId, Map<String, String> attributes, Instant at);

    String resolveScope(Strategy s, String userId, Map<String,String> attrs);

    OperationResult<StrategyResponse> create(CreateStrategyRequest req);
    StrategyResponse getById(Long id);
    List<StrategyResponse> list(Boolean enabled, Boolean isDefault);
    StrategyResponse deactivate(Long id);
    StrategyResponse setDefault(Long id);
}
