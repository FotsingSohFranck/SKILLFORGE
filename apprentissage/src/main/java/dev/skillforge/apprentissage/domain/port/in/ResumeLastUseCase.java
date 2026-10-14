package dev.skillforge.apprentissage.domain.port.in;

import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.ResumePointer;
import reactor.core.publisher.Mono;

/** « Reprendre » : dernier parcours actif de l'apprenant et sa position (vide si aucune activité). */
public interface ResumeLastUseCase {
    Mono<ResumePointer> resume(LearnerId learner);
}
