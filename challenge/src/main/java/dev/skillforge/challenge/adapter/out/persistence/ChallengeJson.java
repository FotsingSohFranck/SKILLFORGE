package dev.skillforge.challenge.adapter.out.persistence;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/** Sérialisation JSON (colonnes {@code jsonb}) partagée du contexte challenge — Jackson 3. */
final class ChallengeJson {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private ChallengeJson() {
    }

    static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Sérialisation JSON impossible (challenge)", e);
        }
    }

    static <T> T read(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Lecture JSON impossible (challenge)", e);
        }
    }
}
