package dev.skillforge.lab.domain.port.in;

import dev.skillforge.lab.domain.model.LearnerId;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/** Port entrant : soumettre une épreuve pratique d'un nœud et obtenir un verdict corrigé. */
public interface SubmitPracticalUseCase {

    Mono<PracticalVerdict> submit(LearnerId learner, UUID nodeId, List<SubmittedFile> files);
}
