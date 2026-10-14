package dev.skillforge.apprentissage.adapter.in.web.dto;

import java.util.UUID;

/** Représentation web d'une inscription + état de reprise. */
public record EnrollmentDto(
        UUID id,
        UUID roadmapId,
        double progressPercent,
        UUID lastActiveNodeId,
        UUID lastActiveLessonId) {
}
