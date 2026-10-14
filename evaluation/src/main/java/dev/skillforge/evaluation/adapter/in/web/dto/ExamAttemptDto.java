package dev.skillforge.evaluation.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

/** Représentation web d'une tentative d'examen (minuteur via deadlineAt). */
public record ExamAttemptDto(
        UUID id,
        UUID examId,
        Instant startedAt,
        Instant deadlineAt,
        Instant submittedAt,
        double totalScore,
        double maxScore,
        Boolean passed,
        String status) {
}
