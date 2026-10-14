package dev.skillforge.contenu.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Corps de requête : création d'une leçon. */
public record CreateLessonRequest(
        @NotBlank String slug,
        @NotBlank String title,
        Integer position) {
}
