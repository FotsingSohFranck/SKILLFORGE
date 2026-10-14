package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.ExamAttemptId;
import dev.skillforge.evaluation.domain.model.ExamResult;
import dev.skillforge.evaluation.domain.model.LearnerId;
import reactor.core.publisher.Mono;

import java.util.List;

/** Soumettre une tentative : notation par domaine, points faibles, persistance, résultat. */
public interface SubmitExamUseCase {
    Mono<ExamResult> submit(LearnerId learner, ExamAttemptId attempt, List<ExamItemAnswer> answers);
}
