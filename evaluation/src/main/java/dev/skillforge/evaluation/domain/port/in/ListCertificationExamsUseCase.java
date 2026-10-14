package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.ExamSummary;
import reactor.core.publisher.Flux;

import java.util.UUID;

/** Cas d'usage : lister les examens blancs d'une certification. */
public interface ListCertificationExamsUseCase {

    Flux<ExamSummary> list(UUID certificationId);
}
