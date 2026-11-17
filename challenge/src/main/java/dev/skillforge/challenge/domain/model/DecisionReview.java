package dev.skillforge.challenge.domain.model;

/**
 * Entrée du journal de mission (révision) : une décision déjà prise, enrichie du titre du nœud et
 * du libellé du choix. Lecture seule — l'historique est immuable et irréversible (SF-92d).
 */
public record DecisionReview(int seq, String nodeId, String nodeTitle, String choiceId, String choiceLabel) {
}
