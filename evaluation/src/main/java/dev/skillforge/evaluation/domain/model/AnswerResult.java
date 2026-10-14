package dev.skillforge.evaluation.domain.model;

/** Résultat de correction d'une réponse à une question. */
public record AnswerResult(
        QuestionId questionId,
        OptionId selectedOptionId,
        boolean correct,
        OptionId correctOptionId,
        String justification) {
}
