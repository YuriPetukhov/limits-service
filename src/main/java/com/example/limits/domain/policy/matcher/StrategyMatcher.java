package com.example.limits.domain.policy.matcher;

import com.example.limits.domain.entity.Strategy;

import java.util.Map;

public interface StrategyMatcher {
    boolean matches(Strategy s, Map<String, String> attributes);
}
