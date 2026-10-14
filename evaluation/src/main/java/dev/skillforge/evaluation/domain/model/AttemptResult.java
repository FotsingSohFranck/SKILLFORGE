package dev.skillforge.evaluation.domain.model;

import java.util.List;

/** Résultat d'une tentative de quiz : score, score max et détail par question. */
public record AttemptResult(
        QuizId quizId,
        int score,
        int maxScore,
        List<AnswerResult> answers) {
}
