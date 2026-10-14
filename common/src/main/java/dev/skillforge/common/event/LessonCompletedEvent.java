package dev.skillforge.common.event;

import java.util.UUID;

/** Événement d'intégration : une leçon a été complétée (publié par apprentissage). */
public record LessonCompletedEvent(UUID learnerId, UUID roadmapId, UUID nodeId, UUID lessonId) {
}
