package dev.skillforge.evaluation.domain.port.out;

import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.port.in.ExamImport;
import reactor.core.publisher.Mono;

/** Port sortant : persistance idempotente d'un examen (upsert par id ; items remplacés). */
public interface ExamImporter {
    Mono<ExamId> importExam(ExamImport command);
}
