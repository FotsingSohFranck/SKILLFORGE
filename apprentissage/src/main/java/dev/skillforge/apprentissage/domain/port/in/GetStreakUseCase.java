package dev.skillforge.apprentissage.domain.port.in;

import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.Streak;
import reactor.core.publisher.Mono;

/** Obtient la série d'apprentissage d'un apprenant (vide → série à 0). */
public interface GetStreakUseCase {
    Mono<Streak> get(LearnerId learner);
}
