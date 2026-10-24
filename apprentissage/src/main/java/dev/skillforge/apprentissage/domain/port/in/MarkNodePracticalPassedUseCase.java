package dev.skillforge.apprentissage.domain.port.in;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port entrant : enregistrer qu'un apprenant a réussi l'épreuve pratique d'un nœud, et recalculer
 * la progression en conséquence (déclenché par {@code PracticalPassedEvent} émis par le lab).
 */
public interface MarkNodePracticalPassedUseCase {

    Mono<Void> markPassed(UUID learnerId, UUID nodeId);
}
