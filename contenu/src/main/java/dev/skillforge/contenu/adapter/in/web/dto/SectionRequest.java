package dev.skillforge.contenu.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/** Corps de requête : création/édition d'une section. */
public record SectionRequest(
        @NotBlank String type,
        Integer position,
        String title,
        String body,
        String codeLanguage,
        String diagramFormat,
        String solution,
        UUID quizId) {
}
