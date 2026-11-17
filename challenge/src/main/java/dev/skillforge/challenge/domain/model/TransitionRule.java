package dev.skillforge.challenge.domain.model;

/**
 * Règle de sélection de la prochaine situation. {@code whenState} est une condition sur l'état de
 * simulation interprétée par le moteur (SF-92a-2) ; {@code "default"} ou vide = transition par
 * défaut. {@code nextNodeId} référence un nœud du scénario.
 */
public record TransitionRule(String whenState, String nextNodeId) {
}
