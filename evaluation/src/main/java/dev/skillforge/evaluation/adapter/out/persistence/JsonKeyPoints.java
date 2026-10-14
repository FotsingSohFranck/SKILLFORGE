package dev.skillforge.evaluation.adapter.out.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/** Conversion des points clés entre {@code List<String>} et JSON (colonne {@code jsonb}). */
final class JsonKeyPoints {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> LIST_OF_STRING = new TypeReference<>() {
    };

    private JsonKeyPoints() {
    }

    static String toJson(List<String> keyPoints) {
        try {
            return MAPPER.writeValueAsString(keyPoints == null ? List.of() : keyPoints);
        } catch (Exception e) {
            throw new IllegalStateException("Sérialisation des points clés impossible", e);
        }
    }

    static List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(json, LIST_OF_STRING);
        } catch (Exception e) {
            throw new IllegalStateException("Lecture des points clés impossible", e);
        }
    }
}
