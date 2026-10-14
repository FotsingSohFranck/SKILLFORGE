package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.Enrollment;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import dev.skillforge.apprentissage.domain.port.in.MarkLessonViewedUseCase;
import dev.skillforge.apprentissage.domain.port.out.EnrollmentStore;
import dev.skillforge.apprentissage.domain.port.out.LessonProgressStore;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;

public class MarkLessonViewedService implements MarkLessonViewedUseCase {

    private final EnrollmentStore enrollments;
    private final LessonProgressStore lessonProgress;
    private final ProgressRecalculator recalculator;
    private final StreakUpdater streakUpdater;

    public MarkLessonViewedService(EnrollmentStore enrollments, LessonProgressStore lessonProgress,
                                   ProgressRecalculator recalculator, StreakUpdater streakUpdater) {
        this.enrollments = enrollments;
        this.lessonProgress = lessonProgress;
        this.recalculator = recalculator;
        this.streakUpdater = streakUpdater;
    }

    @Override
    public Mono<Void> markViewed(LearnerId learner, RoadmapId roadmap, NodeId node, LessonId lesson) {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now();
        return resolve(learner, roadmap)
                .flatMap(enrollment -> lessonProgress.markViewed(enrollment.id(), node, lesson, now)
                        .then(recalculator.recalculate(enrollment, node, lesson, now))
                        .then(streakUpdater.record(learner, today)));
    }

    private Mono<Enrollment> resolve(LearnerId learner, RoadmapId roadmap) {
        return enrollments.findByLearnerAndRoadmap(learner, roadmap)
                .switchIfEmpty(Mono.defer(() -> enrollments.create(learner, roadmap)));
    }
}
