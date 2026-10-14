package dev.skillforge.evaluation.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.MockExam;
import dev.skillforge.evaluation.domain.port.in.GetExamUseCase;
import dev.skillforge.evaluation.domain.port.out.ExamRepository;
import reactor.core.publisher.Mono;

public class GetExamService implements GetExamUseCase {

    private final ExamRepository exams;

    public GetExamService(ExamRepository exams) {
        this.exams = exams;
    }

    @Override
    public Mono<MockExam> get(ExamId exam) {
        return exams.findById(exam)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "EXAM_NOT_FOUND", "Examen introuvable : " + exam.value())));
    }
}
