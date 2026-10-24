package dev.skillforge.common.event;

import java.util.UUID;

/** Événement d'intégration : une épreuve pratique de nœud a été réussie (publié par le contexte lab). */
public record PracticalPassedEvent(UUID learnerId, UUID nodeId, UUID taskId) {
}
