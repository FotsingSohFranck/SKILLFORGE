package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.QuizId;
import reactor.core.publisher.Mono;

/** Importer (créer/mettre à jour) un quiz complet. */
public interface IngestQuizUseCase {
    Mono<QuizId> ingest(QuizImport command);
}
