package dev.skillforge.apprentissage.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Corps de requête d'une activité sur une leçon (vue / complétion). */
public record ActivityRequest(
        @NotNull UUID roadmapId,
        @NotNull UUID nodeId,
        @NotNull UUID lessonId) {
}
