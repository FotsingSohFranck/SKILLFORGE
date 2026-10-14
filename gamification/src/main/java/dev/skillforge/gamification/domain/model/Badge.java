package dev.skillforge.gamification.domain.model;

import java.util.Map;

/** Définition d'un badge et sa règle d'obtention paramétrable ({@code ruleConfig}). */
public record Badge(
        BadgeId id,
        String code,
        String name,
        String ruleType,
        Map<String, Object> ruleConfig) {

    /** Lit un entier de la configuration de règle (ex. {@code days}, {@code xp}), 0 si absent. */
    public int intConfig(String key) {
        Object value = ruleConfig == null ? null : ruleConfig.get(key);
        return value instanceof Number number ? number.intValue() : 0;
    }
}
