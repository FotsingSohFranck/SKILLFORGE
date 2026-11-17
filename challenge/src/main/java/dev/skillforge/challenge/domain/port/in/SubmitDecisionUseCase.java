package dev.skillforge.challenge.domain.port.in;

import dev.skillforge.challenge.domain.model.AttemptView;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Soumet une décision (choix) sur le nœud courant et fait avancer la simulation. */
public interface SubmitDecisionUseCase {

    Mono<AttemptView> submit(UUID learnerId, UUID attemptId, String choiceId);
}
