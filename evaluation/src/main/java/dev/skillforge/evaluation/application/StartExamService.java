package dev.skillforge.evaluation.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.evaluation.domain.model.ExamAttempt;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.port.in.StartExamUseCase;
import dev.skillforge.evaluation.domain.port.out.ExamAttemptStore;
import dev.skillforge.evaluation.domain.port.out.ExamRepository;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

public class StartExamService implements StartExamUseCase {

    private final ExamRepository exams;
    private final ExamAttemptStore attempts;

    public StartExamService(ExamRepository exams, ExamAttemptStore attempts) {
        this.exams = exams;
        this.attempts = attempts;
    }

    @Override
    public Mono<ExamAttempt> start(LearnerId learner, ExamId exam) {
        return exams.findById(exam)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "EXAM_NOT_FOUND", "Examen introuvable : " + exam.value())))
                .flatMap(loaded -> {
                    Instant now = Instant.now();
                    Instant deadline = now.plus(Duration.ofMinutes(loaded.durationMinutes()));
                    return attempts.start(learner, exam, now, deadline);
                });
    }
}
