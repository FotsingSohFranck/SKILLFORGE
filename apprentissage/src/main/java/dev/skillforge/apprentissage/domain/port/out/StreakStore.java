package dev.skillforge.apprentissage.domain.port.out;

import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.Streak;
import reactor.core.publisher.Mono;

/** Port sortant : persistance des séries d'apprentissage. */
public interface StreakStore {

    Mono<Streak> find(LearnerId learner);

    Mono<Void> save(LearnerId learner, Streak streak);
}
