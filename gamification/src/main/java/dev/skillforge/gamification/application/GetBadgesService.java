package dev.skillforge.gamification.application;

import dev.skillforge.gamification.domain.model.EarnedBadge;
import dev.skillforge.gamification.domain.model.LearnerId;
import dev.skillforge.gamification.domain.port.in.GetBadgesUseCase;
import dev.skillforge.gamification.domain.port.out.EarnedBadgeStore;
import reactor.core.publisher.Flux;

public class GetBadgesService implements GetBadgesUseCase {

    private final EarnedBadgeStore earned;

    public GetBadgesService(EarnedBadgeStore earned) {
        this.earned = earned;
    }

    @Override
    public Flux<EarnedBadge> get(LearnerId learner) {
        return earned.findByLearner(learner);
    }
}
