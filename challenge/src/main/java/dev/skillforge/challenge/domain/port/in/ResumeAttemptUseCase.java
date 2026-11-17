package dev.skillforge.challenge.domain.port.in;

import dev.skillforge.challenge.domain.model.AttemptView;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Reprise : tentative en cours de l'apprenant sur ce nœud, ou vide s'il n'y en a pas (SF-92d). */
public interface ResumeAttemptUseCase {

    Mono<AttemptView> currentForNode(UUID learnerId, UUID nodeId);
}
