package dev.skillforge.evaluation.application;

import dev.skillforge.evaluation.domain.model.DomainResult;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.port.in.GetRevisionUseCase;
import dev.skillforge.evaluation.domain.port.out.ExamAttemptStore;
import reactor.core.publisher.Flux;

/** Révision ciblée : domaines faibles de la dernière tentative soumise. */
public class GetRevisionService implements GetRevisionUseCase {

    private final ExamAttemptStore attempts;

    public GetRevisionService(ExamAttemptStore attempts) {
        this.attempts = attempts;
    }

    @Override
    public Flux<DomainResult> weakDomains(LearnerId learner, ExamId exam) {
        return attempts.lastSubmitted(learner, exam)
                .flatMapMany(attempt -> attempts.domainResults(attempt.id())
                        .filter(DomainResult::weak));
    }
}
