package dev.skillforge.challenge.domain.model;

import java.util.UUID;

/** Référence d'une version publiée : l'identifiant du scénario et le numéro de version créé. */
public record ScenarioRef(UUID scenarioId, int version) {
}
