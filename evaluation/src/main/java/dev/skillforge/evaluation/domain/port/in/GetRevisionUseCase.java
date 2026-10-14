package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.DomainResult;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.LearnerId;
import reactor.core.publisher.Flux;

/** Révision ciblée : domaines faibles de la dernière tentative soumise (à retravailler en priorité). */
public interface GetRevisionUseCase {
    Flux<DomainResult> weakDomains(LearnerId learner, ExamId exam);
}
