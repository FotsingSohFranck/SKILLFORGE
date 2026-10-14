package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.ExamAttempt;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.LearnerId;
import reactor.core.publisher.Mono;

/** Démarrer une tentative d'examen : crée la tentative avec sa deadline (minuteur). */
public interface StartExamUseCase {
    Mono<ExamAttempt> start(LearnerId learner, ExamId exam);
}
