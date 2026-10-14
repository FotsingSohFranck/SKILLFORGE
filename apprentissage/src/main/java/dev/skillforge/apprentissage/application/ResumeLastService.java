package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.ResumePointer;
import dev.skillforge.apprentissage.domain.port.in.ResumeLastUseCase;
import dev.skillforge.apprentissage.domain.port.out.EnrollmentStore;
import reactor.core.publisher.Mono;

public class ResumeLastService implements ResumeLastUseCase {

    private final EnrollmentStore enrollments;

    public ResumeLastService(EnrollmentStore enrollments) {
        this.enrollments = enrollments;
    }

    @Override
    public Mono<ResumePointer> resume(LearnerId learner) {
        return enrollments.findLastActive(learner)
                .map(enrollment -> new ResumePointer(
                        enrollment.roadmapId(),
                        enrollment.lastActiveNodeId(),
                        enrollment.lastActiveLessonId()));
    }
}
