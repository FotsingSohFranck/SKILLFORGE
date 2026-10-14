package dev.skillforge.evaluation.domain.model;

import java.util.List;

/** Résultat d'une tentative d'examen : score total, réussite, expiration et détail par domaine. */
public record ExamResult(
        ExamAttemptId attemptId,
        double totalScore,
        double maxScore,
        boolean passed,
        boolean expired,
        List<DomainResult> domains) {
}
