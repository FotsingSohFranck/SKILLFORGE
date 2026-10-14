package dev.skillforge.evaluation.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

/** Quiz exposé pour le passage. */
public record QuizDto(
        UUID id,
        String title,
        String description,
        List<QuestionDto> questions) {
}
