package dev.skillforge.common.event;

import java.util.UUID;

/** Événement d'intégration : un parcours a été complété à 100 % (publié par apprentissage). */
public record RoadmapCompletedEvent(UUID learnerId, UUID roadmapId) {
}
