package dev.skillforge.evaluation.domain.model;

import java.time.Instant;

/** Tentative d'examen : minuteur (deadline), score total et statut. */
public record ExamAttempt(
        ExamAttemptId id,
        ExamId examId,
        LearnerId learnerId,
        Instant startedAt,
        Instant deadlineAt,
        Instant submittedAt,
        double totalScore,
        double maxScore,
        Boolean passed,
        String status) {
}
