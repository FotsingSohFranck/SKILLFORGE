package dev.skillforge.evaluation.domain.port.out;

import dev.skillforge.evaluation.domain.model.DomainResult;
import dev.skillforge.evaluation.domain.model.ExamAttempt;
import dev.skillforge.evaluation.domain.model.ExamAttemptId;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.LearnerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

/** Port sortant : cycle de vie des tentatives d'examen et de leurs résultats par domaine. */
public interface ExamAttemptStore {

    Mono<ExamAttempt> start(LearnerId learner, ExamId exam, Instant startedAt, Instant deadlineAt);

    Mono<ExamAttempt> findById(ExamAttemptId attempt);

    /** Finalise une tentative (score, statut) et enregistre les résultats par domaine. */
    Mono<Void> complete(ExamAttemptId attempt, Instant submittedAt, double totalScore,
                        double maxScore, boolean passed, String status, List<DomainResult> domains);

    Flux<ExamAttempt> history(LearnerId learner, ExamId exam);

    /** Dernière tentative soumise (pour la révision ciblée) ; vide si aucune. */
    Mono<ExamAttempt> lastSubmitted(LearnerId learner, ExamId exam);

    Flux<DomainResult> domainResults(ExamAttemptId attempt);
}
