package dev.skillforge.evaluation.domain.model;

/** Résumé d'un examen blanc (liste par certification, sans les items). */
public record ExamSummary(
        ExamId id,
        String title,
        String format,
        int durationMinutes) {
}
