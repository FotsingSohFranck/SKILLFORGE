package dev.skillforge.gamification.domain.port.in;

import dev.skillforge.gamification.domain.model.EarnedBadge;
import dev.skillforge.gamification.domain.model.LearnerId;
import reactor.core.publisher.Flux;

/** Lister les badges obtenus par un apprenant. */
public interface GetBadgesUseCase {
    Flux<EarnedBadge> get(LearnerId learner);
}
