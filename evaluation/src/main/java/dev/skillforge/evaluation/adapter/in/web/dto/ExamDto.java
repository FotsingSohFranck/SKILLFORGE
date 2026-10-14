package dev.skillforge.evaluation.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

/** Examen exposé pour le passage. */
public record ExamDto(
        UUID id,
        UUID certificationId,
        String title,
        String format,
        int durationMinutes,
        List<ExamItemDto> items) {
}
