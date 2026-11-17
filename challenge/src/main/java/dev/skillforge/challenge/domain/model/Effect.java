package dev.skillforge.challenge.domain.model;

/**
 * Effet d'un choix sur une variable de l'état de simulation (ex. {@code risk += 1}).
 * Les effets sont appliqués par le moteur (SF-92a-2) ; en SF-92a-1 ils ne sont que modélisés
 * et validés structurellement.
 */
public record Effect(String variable, EffectOp op, int value) {
}
