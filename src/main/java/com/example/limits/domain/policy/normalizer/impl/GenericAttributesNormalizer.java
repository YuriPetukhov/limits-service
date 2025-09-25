package com.example.limits.domain.policy.normalizer.impl;

import com.example.limits.domain.policy.contract.AttrContract;
import com.example.limits.domain.policy.normalizer.AttributesNormalizer;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Назначение:
 *  - Нормализует входные атрибуты в соответствии с атрибутным контрактом {@link AttrContract}:
 *      1) применяет алиасы ключей (aliases);
 *      2) приводит регистр ключей (KEEP/LOWER/UPPER);
 *      3) отфильтровывает ключи не из {@code allowedKeys} (если список задан);
 *      4) триммит и отбрасывает пустые ключи/значения;
 *      5) обрезает длину значения до maxLen (override для ключа или defaultMaxLen).
 *
 *  - Проверяет наличие обязательных ключей ({@code requiredKeys}).
 *
 * Гарантии:
 *  - Порядок обхода исходной map сохраняется (LinkedHashMap).
 *  - Результат {@link #normalize(Map, AttrContract)} — неизменяемая Map.
 */
@Component
public class GenericAttributesNormalizer implements AttributesNormalizer {

    /**
     * Нормализует входные атрибуты согласно контракту.
     *
     * @param inputAttributes     исходные атрибуты запроса (может быть null)
     * @param contractOrNull      контракт; если null — применяется {@link AttrContract#permissive()}
     * @return неизменяемая Map нормализованных атрибутов
     */
    @Override
    public Map<String, String> normalize(Map<String, String> inputAttributes, AttrContract contractOrNull) {
        if (inputAttributes == null || inputAttributes.isEmpty()) {
            return Map.of();
        }
        AttrContract contract = (contractOrNull == null) ? AttrContract.permissive() : contractOrNull;

        Map<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : inputAttributes.entrySet()) {
            String rawKey = trimToNull(entry.getKey());
            if (rawKey == null) {
                continue;
            }

            String canonicalKey = canonicalizeKey(rawKey, contract);

            if (!contract.allowedKeys().isEmpty() && !contract.allowedKeys().contains(canonicalKey)) {
                continue;
            }

            String rawValue = trimToNull(entry.getValue());
            if (rawValue == null) {
                continue;
            }

            int maxLen = contract.maxLenOverrides().getOrDefault(canonicalKey, contract.defaultMaxLen());
            String clampedValue = (maxLen > 0 && rawValue.length() > maxLen)
                    ? rawValue.substring(0, maxLen)
                    : rawValue;

            normalized.put(canonicalKey, clampedValue);
        }
        return Collections.unmodifiableMap(normalized);
    }

    /**
     * Валидирует наличие всех обязательных ключей из контракта.
     *
     * @param normalizedAttributes результат {@link #normalize(Map, AttrContract)}
     * @param contractOrNull       контракт; если null или пустые requiredKeys — возвращается пустой список
     * @return список недостающих обязательных ключей; пустой если всё ок
     */
    @Override
    public List<String> validateRequired(Map<String, String> normalizedAttributes, AttrContract contractOrNull) {
        if (contractOrNull == null || contractOrNull.requiredKeys().isEmpty()) {
            return List.of();
        }
        List<String> missing = new ArrayList<>();
        for (String requiredKey : contractOrNull.requiredKeys()) {
            if (normalizedAttributes.get(requiredKey) == null) {
                missing.add(requiredKey);
            }
        }
        return missing;
    }

    /**
     * Применяет алиасы и регист к имени ключа.
     */
    private static String canonicalizeKey(String rawKey, AttrContract contract) {
        String aliasApplied = contract.aliases().getOrDefault(rawKey, rawKey);
        return switch (contract.keyCase()) {
            case LOWER -> aliasApplied.toLowerCase(Locale.ROOT);
            case UPPER -> aliasApplied.toUpperCase(Locale.ROOT);
            case KEEP  -> aliasApplied;
        };
    }

    /**
     * Обрезает пробелы; пустую строку превращает в null.
     */
    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
