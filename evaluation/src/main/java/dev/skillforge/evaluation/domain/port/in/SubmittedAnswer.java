package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.OptionId;
import dev.skillforge.evaluation.domain.model.QuestionId;

/** Réponse soumise par l'apprenant : l'option choisie pour une question. */
public record SubmittedAnswer(QuestionId questionId, OptionId optionId) {
}
