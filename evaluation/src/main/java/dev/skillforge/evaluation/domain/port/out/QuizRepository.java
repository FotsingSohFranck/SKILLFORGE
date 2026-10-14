package dev.skillforge.evaluation.domain.port.out;

import dev.skillforge.evaluation.domain.model.Quiz;
import dev.skillforge.evaluation.domain.model.QuizId;
import reactor.core.publisher.Mono;

/** Port sortant : lecture d'un quiz complet (avec bonnes réponses, pour la correction). */
public interface QuizRepository {
    Mono<Quiz> findById(QuizId quiz);
}
