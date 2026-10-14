package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.AttemptResult;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.model.QuizId;
import reactor.core.publisher.Mono;

import java.util.List;

/** Soumettre une tentative de quiz : corrige, persiste, renvoie le résultat détaillé. */
public interface SubmitAttemptUseCase {
    Mono<AttemptResult> submit(LearnerId learner, QuizId quiz, List<SubmittedAnswer> answers);
}
