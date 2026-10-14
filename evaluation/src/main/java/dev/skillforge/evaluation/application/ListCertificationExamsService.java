package dev.skillforge.evaluation.application;

import dev.skillforge.evaluation.domain.model.ExamSummary;
import dev.skillforge.evaluation.domain.port.in.ListCertificationExamsUseCase;
import dev.skillforge.evaluation.domain.port.out.ExamRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/** Liste les examens blancs d'une certification. */
public class ListCertificationExamsService implements ListCertificationExamsUseCase {

    private final ExamRepository exams;

    public ListCertificationExamsService(ExamRepository exams) {
        this.exams = exams;
    }

    @Override
    public Flux<ExamSummary> list(UUID certificationId) {
        return exams.findByCertification(certificationId);
    }
}
