package dev.skillforge.common.api;

/**
 * Enveloppe de réponse standard réutilisée par toute l'API (cf. docs/ARCHITECTURE.md §3.6).
 * Exactement l'un de {@code data} (succès) ou {@code error} (échec) est non nul ;
 * {@code meta} n'est présent que pour les réponses paginées.
 *
 * @param <T> type du payload
 */
public record ApiResponse<T>(T data, PageMeta meta, ApiError error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, null, null);
    }

    public static <T> ApiResponse<T> page(T data, PageMeta meta) {
        return new ApiResponse<>(data, meta, null);
    }

    public static ApiResponse<Object> error(ApiError error) {
        return new ApiResponse<>(null, null, error);
    }
}
