package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.Enrollment;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.port.in.ListEnrollmentsUseCase;
import dev.skillforge.apprentissage.domain.port.out.EnrollmentStore;
import reactor.core.publisher.Flux;

/** Liste les inscriptions de l'apprenant courant. */
public class ListEnrollmentsService implements ListEnrollmentsUseCase {

    private final EnrollmentStore enrollments;

    public ListEnrollmentsService(EnrollmentStore enrollments) {
        this.enrollments = enrollments;
    }

    @Override
    public Flux<Enrollment> list(LearnerId learner) {
        return enrollments.findByLearner(learner);
    }
}
