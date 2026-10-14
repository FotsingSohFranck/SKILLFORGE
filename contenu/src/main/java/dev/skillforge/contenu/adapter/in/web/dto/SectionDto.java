package dev.skillforge.contenu.adapter.in.web.dto;

import java.util.UUID;

/** Représentation web d'une section typée. */
public record SectionDto(
        UUID id,
        String type,
        int position,
        String title,
        String body,
        String codeLanguage,
        String diagramFormat,
        String solution,
        UUID quizId) {
}
