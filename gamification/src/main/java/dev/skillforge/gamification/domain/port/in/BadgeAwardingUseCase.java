package dev.skillforge.gamification.domain.port.in;

import dev.skillforge.gamification.domain.model.LearnerId;
import reactor.core.publisher.Mono;

/** Évaluation et attribution des badges déclenchées par les événements (idempotent). */
public interface BadgeAwardingUseCase {

    Mono<Void> onNodeCompleted(LearnerId learner);

    Mono<Void> onRoadmapCompleted(LearnerId learner);

    Mono<Void> onExamPassed(LearnerId learner);

    Mono<Void> onStreak(LearnerId learner, int currentLength);

    /** Réévalue les badges de seuil d'XP après un gain (lit l'XP totale courante). */
    Mono<Void> onXpChanged(LearnerId learner);
}
