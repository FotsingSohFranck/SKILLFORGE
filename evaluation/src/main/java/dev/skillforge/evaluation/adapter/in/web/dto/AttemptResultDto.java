package dev.skillforge.evaluation.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

/** Résultat de correction d'une tentative. */
public record AttemptResultDto(
        UUID quizId,
        int score,
        int maxScore,
        List<AnswerResultDto> answers) {

    public record AnswerResultDto(
            UUID questionId,
            UUID selectedOptionId,
            boolean correct,
            UUID correctOptionId,
            String justification) {
    }
}
