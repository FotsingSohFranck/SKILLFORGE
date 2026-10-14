package dev.skillforge.evaluation.domain.model;

import java.util.List;

/** Quiz : un ensemble ordonné de questions. */
public record Quiz(
        QuizId id,
        String title,
        String description,
        List<Question> questions) {
}
