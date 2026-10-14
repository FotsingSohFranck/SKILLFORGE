package dev.skillforge.evaluation.application;

import dev.skillforge.evaluation.domain.model.ExamAttempt;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.port.in.GetExamHistoryUseCase;
import dev.skillforge.evaluation.domain.port.out.ExamAttemptStore;
import reactor.core.publisher.Flux;

public class GetExamHistoryService implements GetExamHistoryUseCase {

    private final ExamAttemptStore attempts;

    public GetExamHistoryService(ExamAttemptStore attempts) {
        this.attempts = attempts;
    }

    @Override
    public Flux<ExamAttempt> history(LearnerId learner, ExamId exam) {
        return attempts.history(learner, exam);
    }
}
