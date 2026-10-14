package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.MockExam;
import reactor.core.publisher.Mono;

/** Obtenir un examen pour passage (404 si absent). Le web masque bonnes réponses/solutions. */
public interface GetExamUseCase {
    Mono<MockExam> get(ExamId exam);
}
