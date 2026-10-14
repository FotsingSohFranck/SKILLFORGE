package dev.skillforge.gamification.event;

import dev.skillforge.common.event.LessonCompletedEvent;
import dev.skillforge.common.event.NodeCompletedEvent;
import dev.skillforge.gamification.adapter.in.event.GamificationEventListener;
import dev.skillforge.gamification.domain.model.LearnerId;
import dev.skillforge.gamification.domain.port.in.AwardXpUseCase;
import dev.skillforge.gamification.domain.port.in.BadgeAwardingUseCase;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GamificationEventListenerTest {

    private int lastAmount = -1;
    private String lastReason;

    private final AwardXpUseCase award = new AwardXpUseCase() {
        @Override
        public Mono<Void> award(LearnerId learner, int amount, String reason, String sourceType, UUID sourceId) {
            lastAmount = amount;
            lastReason = reason;
            return Mono.empty();
        }
    };

    /** Badges no-op pour ce test (on ne teste que l'attribution d'XP). */
    private final BadgeAwardingUseCase badges = new BadgeAwardingUseCase() {
        @Override
        public Mono<Void> onNodeCompleted(LearnerId learner) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> onRoadmapCompleted(LearnerId learner) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> onExamPassed(LearnerId learner) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> onStreak(LearnerId learner, int currentLength) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> onXpChanged(LearnerId learner) {
            return Mono.empty();
        }
    };

    @Test
    void lecon_completee_attribue_50_xp() {
        var listener = new GamificationEventListener(award, badges);
        listener.onLessonCompleted(new LessonCompletedEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        assertEquals(50, lastAmount);
        assertEquals("LESSON_COMPLETED", lastReason);
    }

    @Test
    void noeud_complete_attribue_100_xp() {
        var listener = new GamificationEventListener(award, badges);
        listener.onNodeCompleted(new NodeCompletedEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        assertEquals(100, lastAmount);
        assertEquals("NODE_COMPLETED", lastReason);
    }
}
