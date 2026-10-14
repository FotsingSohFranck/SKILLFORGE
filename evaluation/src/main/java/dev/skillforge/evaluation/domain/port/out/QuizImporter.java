package dev.skillforge.evaluation.domain.port.out;

import dev.skillforge.evaluation.domain.model.QuizId;
import dev.skillforge.evaluation.domain.port.in.QuizImport;
import reactor.core.publisher.Mono;

/** Port sortant : persistance idempotente d'un quiz importé (upsert par id). */
public interface QuizImporter {
    Mono<QuizId> importQuiz(QuizImport command);
}
