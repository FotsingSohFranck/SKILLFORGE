package dev.skillforge.common.event;

import java.util.UUID;

/** Événement d'intégration : un Final Challenge de fin de nœud a été réussi (publié par le contexte challenge). */
public record FinalChallengePassedEvent(UUID learnerId, UUID nodeId, UUID scenarioId) {
}
