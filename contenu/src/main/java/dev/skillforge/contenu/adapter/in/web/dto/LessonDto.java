package dev.skillforge.contenu.adapter.in.web.dto;

import java.util.UUID;

/** Représentation web d'une leçon (résumé). */
public record LessonDto(
        UUID id,
        UUID nodeId,
        String slug,
        String title,
        int position) {
}
