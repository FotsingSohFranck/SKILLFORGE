package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import dev.skillforge.apprentissage.domain.model.RoadmapProgress;
import dev.skillforge.apprentissage.domain.port.in.GetRoadmapProgressUseCase;
import dev.skillforge.apprentissage.domain.port.out.EnrollmentStore;
import dev.skillforge.apprentissage.domain.port.out.NodeProgressStore;
import dev.skillforge.common.error.NotFoundException;
import reactor.core.publisher.Mono;

public class GetRoadmapProgressService implements GetRoadmapProgressUseCase {

    private final EnrollmentStore enrollments;
    private final NodeProgressStore nodeProgress;

    public GetRoadmapProgressService(EnrollmentStore enrollments, NodeProgressStore nodeProgress) {
        this.enrollments = enrollments;
        this.nodeProgress = nodeProgress;
    }

    @Override
    public Mono<RoadmapProgress> get(LearnerId learner, RoadmapId roadmap) {
        return enrollments.findByLearnerAndRoadmap(learner, roadmap)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "NOT_ENROLLED", "Apprenant non inscrit à ce parcours.")))
                .flatMap(enrollment -> nodeProgress.findByEnrollment(enrollment.id())
                        .collectList()
                        .map(nodes -> new RoadmapProgress(enrollment, nodes)));
    }
}
