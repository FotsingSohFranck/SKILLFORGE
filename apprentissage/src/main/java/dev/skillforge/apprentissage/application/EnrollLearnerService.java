package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.Enrollment;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import dev.skillforge.apprentissage.domain.port.in.EnrollLearnerUseCase;
import dev.skillforge.apprentissage.domain.port.out.EnrollmentStore;
import reactor.core.publisher.Mono;

public class EnrollLearnerService implements EnrollLearnerUseCase {

    private final EnrollmentStore enrollments;

    public EnrollLearnerService(EnrollmentStore enrollments) {
        this.enrollments = enrollments;
    }

    @Override
    public Mono<Enrollment> enroll(LearnerId learner, RoadmapId roadmap) {
        return enrollments.findByLearnerAndRoadmap(learner, roadmap)
                .switchIfEmpty(Mono.defer(() -> enrollments.create(learner, roadmap)));
    }
}
