package dev.skillforge.gamification.adapter.in.event;

import dev.skillforge.common.event.ExamPassedEvent;
import dev.skillforge.common.event.LessonCompletedEvent;
import dev.skillforge.common.event.NodeCompletedEvent;
import dev.skillforge.common.event.RoadmapCompletedEvent;
import dev.skillforge.common.event.StreakUpdatedEvent;
import dev.skillforge.gamification.domain.model.LearnerId;
import dev.skillforge.gamification.domain.port.in.AwardXpUseCase;
import dev.skillforge.gamification.domain.port.in.BadgeAwardingUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Consomme les événements d'intégration (bus Spring) : attribue l'XP puis évalue les badges.
 * Barème d'XP (constantes ; paramétrable = dette) : leçon 50, nœud 100, examen 200, parcours 500.
 */
@Component
public class GamificationEventListener {

    private static final int LESSON_XP = 50;
    private static final int NODE_XP = 100;
    private static final int EXAM_XP = 200;
    private static final int ROADMAP_XP = 500;

    private final AwardXpUseCase award;
    private final BadgeAwardingUseCase badges;

    public GamificationEventListener(AwardXpUseCase award, BadgeAwardingUseCase badges) {
        this.award = award;
        this.badges = badges;
    }

    @EventListener
    public void onLessonCompleted(LessonCompletedEvent event) {
        LearnerId learner = new LearnerId(event.learnerId());
        award.award(learner, LESSON_XP, "LESSON_COMPLETED", "LESSON", event.lessonId())
                .then(badges.onXpChanged(learner))
                .subscribe();
    }

    @EventListener
    public void onNodeCompleted(NodeCompletedEvent event) {
        LearnerId learner = new LearnerId(event.learnerId());
        award.award(learner, NODE_XP, "NODE_COMPLETED", "NODE", event.nodeId())
                .then(badges.onNodeCompleted(learner))
                .then(badges.onXpChanged(learner))
                .subscribe();
    }

    @EventListener
    public void onRoadmapCompleted(RoadmapCompletedEvent event) {
        LearnerId learner = new LearnerId(event.learnerId());
        award.award(learner, ROADMAP_XP, "ROADMAP_COMPLETED", "ROADMAP", event.roadmapId())
                .then(badges.onRoadmapCompleted(learner))
                .then(badges.onXpChanged(learner))
                .subscribe();
    }

    @EventListener
    public void onExamPassed(ExamPassedEvent event) {
        LearnerId learner = new LearnerId(event.learnerId());
        award.award(learner, EXAM_XP, "EXAM_PASSED", "EXAM", event.examId())
                .then(badges.onExamPassed(learner))
                .then(badges.onXpChanged(learner))
                .subscribe();
    }

    @EventListener
    public void onStreakUpdated(StreakUpdatedEvent event) {
        badges.onStreak(new LearnerId(event.learnerId()), event.currentLength()).subscribe();
    }
}
