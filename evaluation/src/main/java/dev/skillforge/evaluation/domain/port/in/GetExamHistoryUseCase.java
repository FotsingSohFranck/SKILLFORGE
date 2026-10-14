package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.ExamAttempt;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.LearnerId;
import reactor.core.publisher.Flux;

/** Historique des tentatives d'un examen pour un apprenant. */
public interface GetExamHistoryUseCase {
    Flux<ExamAttempt> history(LearnerId learner, ExamId exam);
}
