package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.Enrollment;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.ProgressStatus;
import dev.skillforge.apprentissage.domain.port.out.EnrollmentStore;
import dev.skillforge.apprentissage.domain.port.out.LearningCatalogPort;
import dev.skillforge.apprentissage.domain.port.out.LessonProgressStore;
import dev.skillforge.apprentissage.domain.port.out.NodeProgressStore;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Recalcule et persiste la progression après une activité sur une leçon :
 * % du nœud (leçons complétées / total) + statut, puis % du parcours (nœuds complétés / total)
 * et état de reprise. Renvoie le {@link ProgressOutcome} (statut du nœud + % du parcours).
 */
public class ProgressRecalculator {

    private final EnrollmentStore enrollments;
    private final NodeProgressStore nodeProgress;
    private final LessonProgressStore lessonProgress;
    private final LearningCatalogPort catalog;

    public ProgressRecalculator(EnrollmentStore enrollments, NodeProgressStore nodeProgress,
                                LessonProgressStore lessonProgress, LearningCatalogPort catalog) {
        this.enrollments = enrollments;
        this.nodeProgress = nodeProgress;
        this.lessonProgress = lessonProgress;
        this.catalog = catalog;
    }

    public Mono<ProgressOutcome> recalculate(Enrollment enrollment, NodeId node, LessonId lesson, Instant at) {
        return Mono.zip(
                        lessonProgress.countCompletedInNode(enrollment.id(), node),
                        catalog.countLessonsInNode(node),
                        catalog.completionPolicy(node),
                        nodeProgress.isPracticalPassed(enrollment.id(), node))
                .flatMap(t -> {
                    long done = t.getT1();
                    long total = t.getT2();
                    boolean practicalOk = !t.getT3().requiresPractical() || t.getT4();
                    double nodePercent = total == 0 ? 0.0 : Math.min(100.0, done * 100.0 / total);
                    ProgressStatus status = (total > 0 && done >= total && practicalOk)
                            ? ProgressStatus.COMPLETED : ProgressStatus.IN_PROGRESS;
                    return nodeProgress.upsert(enrollment.id(), node, status, nodePercent, lesson, at)
                            .then(updateRoadmap(enrollment, node, lesson, at))
                            .map(roadmapPercent -> new ProgressOutcome(status, roadmapPercent));
                });
    }

    private Mono<Double> updateRoadmap(Enrollment enrollment, NodeId node, LessonId lesson, Instant at) {
        return Mono.zip(
                        nodeProgress.countCompleted(enrollment.id()),
                        catalog.countNodesInRoadmap(enrollment.roadmapId()))
                .flatMap(counts -> {
                    long completedNodes = counts.getT1();
                    long totalNodes = counts.getT2();
                    double roadmapPercent = totalNodes == 0
                            ? 0.0 : Math.min(100.0, completedNodes * 100.0 / totalNodes);
                    return enrollments.updateProgress(enrollment.id(), node, lesson, roadmapPercent, at)
                            .thenReturn(roadmapPercent);
                });
    }
}
