package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.ExamItemId;
import dev.skillforge.evaluation.domain.model.OptionId;

/**
 * Réponse à un item d'examen : option choisie ({@code MCQ}) ou auto-évaluation
 * {@code completed} (tâche pratique).
 */
public record ExamItemAnswer(
        ExamItemId itemId,
        OptionId selectedOptionId,
        Boolean completed) {
}
