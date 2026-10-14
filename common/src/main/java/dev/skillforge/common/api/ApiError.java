package dev.skillforge.common.api;

import java.util.List;

/**
 * Bloc d'erreur normalisé de l'enveloppe {@link ApiResponse}.
 *
 * @param code    code métier stable (ex. {@code ROADMAP_NOT_FOUND})
 * @param message message lisible (FR) — jamais de stacktrace
 * @param details erreurs de validation champ par champ (vide si non applicable)
 */
public record ApiError(String code, String message, List<FieldError> details) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, List.of());
    }

    public static ApiError of(String code, String message, List<FieldError> details) {
        return new ApiError(code, message, details);
    }

    /** Détail d'erreur de validation pour un champ donné. */
    public record FieldError(String field, String message) {
    }
}
