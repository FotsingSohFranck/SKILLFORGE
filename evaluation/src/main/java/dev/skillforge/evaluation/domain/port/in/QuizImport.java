package dev.skillforge.evaluation.domain.port.in;

import java.util.List;
import java.util.UUID;

/**
 * Commande d'import d'un quiz (pivot). L'{@code id} est fourni pour rester cohérent avec le
 * {@code quiz_id} référencé par une section de contenu (upsert par id, idempotent).
 */
public record QuizImport(
        UUID id,
        UUID nodeId,
        UUID lessonId,
        String title,
        String description,
        List<QuestionImport> questions) {

    public record QuestionImport(
            String type,
            String prompt,
            String justification,
            Integer position,
            List<OptionImport> options) {
    }

    public record OptionImport(
            String label,
            Boolean correct,
            Integer position) {
    }
}
