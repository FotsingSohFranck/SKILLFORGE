package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.Enrollment;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.ProgressStatus;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import dev.skillforge.apprentissage.domain.port.in.CompleteLessonUseCase;
import dev.skillforge.apprentissage.domain.port.out.EnrollmentStore;
import dev.skillforge.apprentissage.domain.port.out.LearningEventPublisher;
import dev.skillforge.apprentissage.domain.port.out.LessonProgressStore;
import dev.skillforge.common.event.LessonCompletedEvent;
import dev.skillforge.common.event.NodeCompletedEvent;
import dev.skillforge.common.event.RoadmapCompletedEvent;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;

public class CompleteLessonService implements CompleteLessonUseCase {

    private final EnrollmentStore enrollments;
    private final LessonProgressStore lessonProgress;
    private final ProgressRecalculator recalculator;
    private final StreakUpdater streakUpdater;
    private final LearningEventPublisher events;

    public CompleteLessonService(EnrollmentStore enrollments, LessonProgressStore lessonProgress,
                                 ProgressRecalculator recalculator, StreakUpdater streakUpdater,
                                 LearningEventPublisher events) {
        this.enrollments = enrollments;
        this.lessonProgress = lessonProgress;
        this.recalculator = recalculator;
        this.streakUpdater = streakUpdater;
        this.events = events;
    }

    @Override
    public Mono<Void> complete(LearnerId learner, RoadmapId roadmap, NodeId node, LessonId lesson) {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now();
        return resolve(learner, roadmap)
                .flatMap(enrollment -> lessonProgress.markCompleted(enrollment.id(), node, lesson, now)
                        .then(recalculator.recalculate(enrollment, node, lesson, now))
                        .flatMap(outcome -> streakUpdater.record(learner, today)
                                .then(events.publish(new LessonCompletedEvent(
                                        learner.value(), roadmap.value(), node.value(), lesson.value())))
                                .then(publishNodeCompleted(outcome.nodeStatus(), learner, roadmap, node))
                                .then(publishRoadmapCompleted(outcome.roadmapPercent(), learner, roadmap))));
    }

    private Mono<Void> publishNodeCompleted(ProgressStatus status, LearnerId learner, RoadmapId roadmap, NodeId node) {
        if (status == ProgressStatus.COMPLETED) {
            return events.publish(new NodeCompletedEvent(learner.value(), roadmap.value(), node.value()));
        }
        return Mono.empty();
    }

    private Mono<Void> publishRoadmapCompleted(double roadmapPercent, LearnerId learner, RoadmapId roadmap) {
        if (roadmapPercent >= 100.0) {
            return events.publish(new RoadmapCompletedEvent(learner.value(), roadmap.value()));
        }
        return Mono.empty();
    }

    private Mono<Enrollment> resolve(LearnerId learner, RoadmapId roadmap) {
        return enrollments.findByLearnerAndRoadmap(learner, roadmap)
                .switchIfEmpty(Mono.defer(() -> enrollments.create(learner, roadmap)));
    }
}
