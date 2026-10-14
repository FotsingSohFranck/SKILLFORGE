package dev.skillforge.evaluation.adapter.in.web.dto;

import java.util.UUID;

/** Résumé d'un examen blanc exposé dans la liste d'une certification. */
public record ExamSummaryDto(UUID id, String title, String format, int durationMinutes) {
}
