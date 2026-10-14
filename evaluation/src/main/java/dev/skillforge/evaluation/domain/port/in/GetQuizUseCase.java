package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.Quiz;
import dev.skillforge.evaluation.domain.model.QuizId;
import reactor.core.publisher.Mono;

/** Obtenir un quiz (404 si absent). Le web masque les bonnes réponses avant correction. */
public interface GetQuizUseCase {
    Mono<Quiz> get(QuizId quiz);
}
