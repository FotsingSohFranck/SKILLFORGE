package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.Streak;
import dev.skillforge.apprentissage.domain.port.out.LearningEventPublisher;
import dev.skillforge.apprentissage.domain.port.out.StreakStore;
import dev.skillforge.common.event.StreakUpdatedEvent;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Met à jour la série d'apprentissage lors d'une activité (fonction pure {@link Streak#registerActivity})
 * puis publie un {@link StreakUpdatedEvent}.
 */
public class StreakUpdater {

    private final StreakStore streaks;
    private final LearningEventPublisher events;

    public StreakUpdater(StreakStore streaks, LearningEventPublisher events) {
        this.streaks = streaks;
        this.events = events;
    }

    public Mono<Void> record(LearnerId learner, LocalDate today) {
        return streaks.find(learner)
                .defaultIfEmpty(Streak.empty())
                .map(streak -> streak.registerActivity(today))
                .flatMap(streak -> streaks.save(learner, streak)
                        .then(events.publish(new StreakUpdatedEvent(learner.value(), streak.currentLength()))));
    }
}
