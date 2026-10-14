package dev.skillforge.evaluation.domain.port.out;

import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.ExamSummary;
import dev.skillforge.evaluation.domain.model.MockExam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Port sortant : lecture d'un examen complet (items + options des QCM, pour la notation). */
public interface ExamRepository {
    Mono<MockExam> findById(ExamId exam);

    /** Résumés des examens blancs d'une certification (référence souple vers catalogue). */
    Flux<ExamSummary> findByCertification(UUID certificationId);
}
