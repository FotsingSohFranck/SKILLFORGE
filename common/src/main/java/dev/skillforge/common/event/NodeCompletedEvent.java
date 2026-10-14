package dev.skillforge.common.event;

import java.util.UUID;

/** Événement d'intégration : un nœud a été entièrement complété (publié par apprentissage). */
public record NodeCompletedEvent(UUID learnerId, UUID roadmapId, UUID nodeId) {
}
