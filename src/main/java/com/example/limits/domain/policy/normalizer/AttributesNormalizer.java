package com.example.limits.domain.policy.normalizer;

import com.example.limits.domain.policy.contract.AttrContract;

import java.util.List;
import java.util.Map;

public interface AttributesNormalizer {
    Map<String, String> normalize(Map<String, String> in, AttrContract contract);
    List<String> validateRequired(Map<String, String> normalized, AttrContract contract);
}
