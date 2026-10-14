package dev.skillforge.common.api;

/**
 * Métadonnées de pagination pour l'enveloppe {@link ApiResponse}.
 *
 * @param page  index de page (0-based)
 * @param size  taille de page
 * @param total nombre total d'éléments
 */
public record PageMeta(int page, int size, long total) {
}
