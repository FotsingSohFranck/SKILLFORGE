package dev.skillforge.evaluation.domain.model;

import java.util.List;
import java.util.UUID;

/** Examen blanc chronométré rattaché à une certification. */
public record MockExam(
        ExamId id,
        UUID certificationId,
        String title,
        String format,
        int durationMinutes,
        Double passingScore,
        List<ExamItem> items) {
}
