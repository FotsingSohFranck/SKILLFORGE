package dev.skillforge.challenge.adapter.out.persistence;

import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Conversion {@link ScenarioDefinition} ⇄ JSON (colonne {@code jsonb}). Mapper dédié Jackson 3
 * ({@code tools.jackson}, le défaut autoconfiguré sous Boot 4, support natif des records).
 */
final class ScenarioDefinitionJson {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private ScenarioDefinitionJson() {
    }

    static String toJson(ScenarioDefinition definition) {
        try {
            return MAPPER.writeValueAsString(definition);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Sérialisation de la définition de scénario impossible", e);
        }
    }

    static ScenarioDefinition fromJson(String json) {
        try {
            return MAPPER.readValue(json, ScenarioDefinition.class);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Lecture de la définition de scénario impossible", e);
        }
    }
}
