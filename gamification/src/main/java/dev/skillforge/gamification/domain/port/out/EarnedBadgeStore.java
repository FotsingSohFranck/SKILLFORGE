package dev.skillforge.gamification.domain.port.out;

import dev.skillforge.gamification.domain.model.BadgeId;
import dev.skillforge.gamification.domain.model.EarnedBadge;
import dev.skillforge.gamification.domain.model.LearnerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Port sortant : badges obtenus (attribution idempotente). */
public interface EarnedBadgeStore {

    Mono<Void> award(LearnerId learner, BadgeId badge);

    Flux<EarnedBadge> findByLearner(LearnerId learner);
}
