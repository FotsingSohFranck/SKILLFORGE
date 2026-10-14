package dev.skillforge.evaluation.domain.model;

/** Résumé d'un défi de code (liste par nœud, sans énoncé ni solution). */
public record ChallengeSummary(
        ChallengeId id,
        String title,
        String language) {
}
