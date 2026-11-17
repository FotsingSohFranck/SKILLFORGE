package dev.skillforge.challenge.domain.model;

/**
 * Condition éliminatoire : si elle est satisfaite, la validation est refusée même si le score
 * global dépasse le seuil (cf. spec §7.6). {@code whenState} est évalué par le moteur (SF-92a-2) ;
 * {@code description} est expliquée dans le bilan final.
 */
public record CriticalCondition(String id, String description, String whenState) {
}
