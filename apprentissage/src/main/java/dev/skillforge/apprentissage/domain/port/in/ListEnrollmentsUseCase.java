package dev.skillforge.apprentissage.domain.port.in;

import dev.skillforge.apprentissage.domain.model.Enrollment;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import reactor.core.publisher.Flux;

/** Cas d'usage : lister les inscriptions de l'apprenant courant (tableau de bord). */
public interface ListEnrollmentsUseCase {

    Flux<Enrollment> list(LearnerId learner);
}
