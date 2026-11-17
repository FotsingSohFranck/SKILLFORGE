package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.Enrollment;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.NodeCompletion;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.ProgressStatus;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import dev.skillforge.apprentissage.domain.port.in.MarkNodeChallengePassedUseCase;
import dev.skillforge.apprentissage.domain.port.out.EnrollmentStore;
import dev.skillforge.apprentissage.domain.port.out.LearningCatalogPort;
import dev.skillforge.apprentissage.domain.port.out.LearningEventPublisher;
import dev.skillforge.apprentissage.domain.port.out.LessonProgressStore;
import dev.skillforge.apprentissage.domain.port.out.NodeProgressStore;
import dev.skillforge.common.event.NodeCompletedEvent;
import dev.skillforge.common.event.RoadmapCompletedEvent;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Applique la réussite d'un Final Challenge à la progression : mémorise le succès sur le nœud,
 * recalcule son statut (leçons terminées + épreuve pratique éventuelle + challenge = COMPLETED),
 * met à jour le % du parcours, et publie {@code NodeCompletedEvent} / {@code RoadmapCompletedEvent}
 * à la transition. Miroir de {@link MarkNodePracticalPassedService}.
 */
public class MarkNodeChallengePassedService implements MarkNodeChallengePassedUseCase {

    private final EnrollmentStore enrollments;
    private final NodeProgressStore nodeProgress;
    private final LessonProgressStore lessonProgress;
    private final LearningCatalogPort catalog;
    private final LearningEventPublisher events;

    public MarkNodeChallengePassedService(EnrollmentStore enrollments, NodeProgressStore nodeProgress,
                                          LessonProgressStore lessonProgress, LearningCatalogPort catalog,
                                          LearningEventPublisher events) {
        this.enrollments = enrollments;
        this.nodeProgress = nodeProgress;
        this.lessonProgress = lessonProgress;
        this.catalog = catalog;
        this.events = events;
    }

    @Override
    public Mono<Void> markPassed(UUID learnerId, UUID nodeId) {
        LearnerId learner = new LearnerId(learnerId);
        NodeId node = new NodeId(nodeId);
        Instant now = Instant.now();
        return catalog.roadmapOfNode(node)
                .flatMap(roadmap -> resolve(learner, roadmap)
                        .flatMap(enr -> apply(learner, roadmap, enr, node, now)));
    }

    private Mono<Void> apply(LearnerId learner, RoadmapId roadmap, Enrollment enr, NodeId node, Instant now) {
        return nodeProgress.status(enr.id(), node).defaultIfEmpty(ProgressStatus.IN_PROGRESS)
                .flatMap(previous -> nodeProgress.markChallengePassed(enr.id(), node, now)
                        .then(Mono.zip(
                                lessonProgress.countCompletedInNode(enr.id(), node),
                                catalog.countLessonsInNode(node),
                                catalog.completionPolicy(node),
                                nodeProgress.isPracticalPassed(enr.id(), node)))
                        .flatMap(t -> {
                            long done = t.getT1();
                            long total = t.getT2();
                            double percent = total == 0 ? 0.0 : Math.min(100.0, done * 100.0 / total);
                            // Le challenge vient d'être réussi ; restent « leçons terminées » et une
                            // éventuelle épreuve pratique requise.
                            boolean complete = NodeCompletion.isComplete(done, total,
                                    t.getT3().requiresPractical(), t.getT4(), false, true);
                            ProgressStatus status = complete
                                    ? ProgressStatus.COMPLETED : ProgressStatus.IN_PROGRESS;
                            boolean newlyCompleted = status == ProgressStatus.COMPLETED
                                    && previous != ProgressStatus.COMPLETED;
                            return nodeProgress.updateStatus(enr.id(), node, status, percent, now)
                                    .then(recomputeRoadmap(enr, now))
                                    .flatMap(roadmapPercent -> emit(newlyCompleted, roadmapPercent, learner, roadmap, node));
                        }));
    }

    private Mono<Double> recomputeRoadmap(Enrollment enr, Instant now) {
        return Mono.zip(
                        nodeProgress.countCompleted(enr.id()),
                        catalog.countNodesInRoadmap(enr.roadmapId()))
                .flatMap(c -> {
                    long completed = c.getT1();
                    long totalNodes = c.getT2();
                    double percent = totalNodes == 0 ? 0.0 : Math.min(100.0, completed * 100.0 / totalNodes);
                    return enrollments.updatePercent(enr.id(), percent, now).thenReturn(percent);
                });
    }

    private Mono<Void> emit(boolean newlyCompleted, double roadmapPercent, LearnerId learner,
                            RoadmapId roadmap, NodeId node) {
        if (!newlyCompleted) {
            return Mono.empty();
        }
        Mono<Void> nodeEvent = events.publish(new NodeCompletedEvent(learner.value(), roadmap.value(), node.value()));
        if (roadmapPercent >= 100.0) {
            return nodeEvent.then(events.publish(new RoadmapCompletedEvent(learner.value(), roadmap.value())));
        }
        return nodeEvent;
    }

    private Mono<Enrollment> resolve(LearnerId learner, RoadmapId roadmap) {
        return enrollments.findByLearnerAndRoadmap(learner, roadmap)
                .switchIfEmpty(Mono.defer(() -> enrollments.create(learner, roadmap)));
    }
}
