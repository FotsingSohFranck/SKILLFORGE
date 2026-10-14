package dev.skillforge.evaluation.domain.model;

/** Résultat d'un domaine d'examen : score, max et indicateur de point faible. */
public record DomainResult(
        ExamDomainId domainId,
        double score,
        double maxScore,
        boolean weak) {
}
