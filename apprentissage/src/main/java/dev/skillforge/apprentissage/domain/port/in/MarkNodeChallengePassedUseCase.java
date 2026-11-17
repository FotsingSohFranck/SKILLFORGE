package dev.skillforge.apprentissage.domain.port.in;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port entrant : enregistrer qu'un apprenant a réussi le Final Challenge d'un nœud, et recalculer la
 * progression en conséquence (déclenché par {@code FinalChallengePassedEvent} émis par le contexte challenge).
 */
public interface MarkNodeChallengePassedUseCase {

    Mono<Void> markPassed(UUID learnerId, UUID nodeId);
}
