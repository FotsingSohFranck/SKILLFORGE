package dev.skillforge.challenge.domain.port.in;

import dev.skillforge.challenge.domain.model.AttemptView;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Lit l'état courant d'une tentative appartenant à l'apprenant (nœud courant ou rapport final). */
public interface GetAttemptUseCase {

    Mono<AttemptView> get(UUID learnerId, UUID attemptId);
}
