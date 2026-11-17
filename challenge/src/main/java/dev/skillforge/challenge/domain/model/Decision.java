package dev.skillforge.challenge.domain.model;

/** Choix enregistré à un nœud, dans l'ordre ({@code seq} croissant). */
public record Decision(int seq, String nodeId, String choiceId) {
}
