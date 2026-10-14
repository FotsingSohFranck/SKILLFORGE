package dev.skillforge.evaluation.domain.port.out;

import dev.skillforge.evaluation.domain.model.AnswerResult;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.model.QuizId;
import reactor.core.publisher.Mono;

import java.util.List;

/** Port sortant : persistance des tentatives de quiz. */
public interface AttemptStore {
    Mono<Void> save(LearnerId learner, QuizId quiz, int score, int maxScore, List<AnswerResult> answers);
}
