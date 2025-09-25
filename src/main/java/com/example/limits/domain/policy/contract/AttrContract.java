package com.example.limits.domain.policy.contract;

import java.util.Map;
import java.util.Set;

/**
 * Контракт атрибутов, полученный из стратегий.
 * Используется нормализатором для:
 * <ul>
 *   <li>фильтрации допустимых ключей (allowedKeys),</li>
 *   <li>проверки обязательных ключей (requiredKeys),</li>
 *   <li>приведения ключей через алиасы и регистр (aliases, keyCase),</li>
 *   <li>ограничения длины значений (defaultMaxLen и maxLenOverrides).</li>
 * </ul>
 *
 * <p><b>Семантика пустых коллекций:</b>
 * <ul>
 *   <li><code>allowedKeys</code> пусто → разрешены любые ключи;</li>
 *   <li><code>requiredKeys</code> пусто → нет обязательных ключей;</li>
 *   <li><code>aliases</code> пусто → алиасы не применяются;</li>
 *   <li><code>maxLenOverrides</code> пусто → действует только defaultMaxLen.</li>
 * </ul>
 *
 * <p>Экземпляр неизменяем (Java record). Для «мягкого» режима используйте {@link #permissive()}.</p>
 */
public record AttrContract(
        /**
         * Набор разрешённых ключей. Пустой набор означает «разрешены любые».
         */
        Set<String> allowedKeys,

        /**
         * Набор обязательных ключей. Пустой набор означает «нет обязательных».
         */
        Set<String> requiredKeys,

        /**
         * Алиасы ключей: входное имя → каноническое имя.
         * Применяется до приведения регистра.
         */
        Map<String, String> aliases,

        /**
         * Как приводить регистр имён ключей после применения алиасов.
         */
        KeyCase keyCase,

        /**
         * Максимальная длина значения по умолчанию (символов), применяемая,
         * если для ключа нет явного переопределения.
         */
        int defaultMaxLen,

        /**
         * Переопределения максимальной длины по ключам: ключ → максимальная длина.
         */
        Map<String, Integer> maxLenOverrides
) {

    /**
     * Перепустительный (пермиссивный) контракт:
     * <ul>
     *   <li>нет ограничений на набор ключей,</li>
     *   <li>нет обязательных ключей,</li>
     *   <li>нет алиасов,</li>
     *   <li>регистр не меняется,</li>
     *   <li>ограничение длины по умолчанию — 256 символов,</li>
     *   <li>без переопределений по ключам.</li>
     * </ul>
     */
    public static AttrContract permissive() {
        return new AttrContract(Set.of(), Set.of(), Map.of(), KeyCase.KEEP, 256, Map.of());
    }

    /**
     * Режим приведения регистра ключей.
     */
    public enum KeyCase { KEEP, LOWER, UPPER }
}
