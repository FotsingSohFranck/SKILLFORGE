package dev.skillforge.contenu.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Corps de requête : édition d'une leçon. */
public record UpdateLessonRequest(
        @NotBlank String slug,
        @NotBlank String title,
        Integer position) {
}
