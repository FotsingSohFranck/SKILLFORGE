package dev.skillforge.lab.domain.port.in;

import dev.skillforge.lab.domain.model.LearnerId;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Port entrant : consulter l'épreuve pratique d'un nœud et l'état de la dernière tentative. */
public interface GetPracticalUseCase {

    Mono<PracticalView> get(LearnerId learner, UUID nodeId);
}
