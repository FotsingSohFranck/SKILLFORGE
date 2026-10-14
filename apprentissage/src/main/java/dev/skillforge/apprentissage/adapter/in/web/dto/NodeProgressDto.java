package dev.skillforge.apprentissage.adapter.in.web.dto;

import java.util.UUID;

/** Représentation web de la progression d'un nœud. */
public record NodeProgressDto(
        UUID nodeId,
        String status,
        double percent,
        UUID lastLessonId) {
}
