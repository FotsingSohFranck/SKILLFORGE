package dev.skillforge.apprentissage.adapter.in.web.dto;

import java.util.UUID;

/** Représentation web du point de reprise globale. */
public record ResumeDto(
        UUID roadmapId,
        UUID nodeId,
        UUID lessonId) {
}
