package dev.skillforge.challenge.domain.port.out;

import dev.skillforge.challenge.domain.model.ChallengeAttempt;
import dev.skillforge.challenge.domain.model.ChallengeResult;
import dev.skillforge.challenge.domain.model.Decision;
import dev.skillforge.challenge.domain.model.SimulationState;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Persistance des tentatives et de leur journal de décisions. */
public interface AttemptStore {

    /** Crée une tentative en cours (état initial, nœud initial) et renvoie l'instance persistée. */
    Mono<ChallengeAttempt> create(UUID scenarioId, int version, UUID nodeId, UUID learnerId,
                                  String currentNodeId, SimulationState initialState);

    /** Charge une tentative (avec ses décisions) si elle appartient à l'apprenant, sinon vide. */
    Mono<ChallengeAttempt> findOwned(UUID attemptId, UUID learnerId);

    /** Enregistre la décision, met à jour l'état et le nœud courant (tentative toujours en cours). */
    Mono<Void> advance(UUID attemptId, Decision decision, SimulationState state, String currentNodeId);

    /** Enregistre la décision finale et clôt la tentative avec son rapport (état final figé). */
    Mono<Void> complete(UUID attemptId, Decision decision, SimulationState finalState, ChallengeResult result);
}
