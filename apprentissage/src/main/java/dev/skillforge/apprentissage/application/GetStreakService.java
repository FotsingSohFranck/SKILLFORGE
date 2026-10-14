package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.Streak;
import dev.skillforge.apprentissage.domain.port.in.GetStreakUseCase;
import dev.skillforge.apprentissage.domain.port.out.StreakStore;
import reactor.core.publisher.Mono;

public class GetStreakService implements GetStreakUseCase {

    private final StreakStore streaks;

    public GetStreakService(StreakStore streaks) {
        this.streaks = streaks;
    }

    @Override
    public Mono<Streak> get(LearnerId learner) {
        return streaks.find(learner).defaultIfEmpty(Streak.empty());
    }
}
