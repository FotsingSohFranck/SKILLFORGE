package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.ExamId;
import reactor.core.publisher.Mono;

/** Importer (créer/mettre à jour) un examen blanc. */
public interface IngestExamUseCase {
    Mono<ExamId> ingest(ExamImport command);
}
