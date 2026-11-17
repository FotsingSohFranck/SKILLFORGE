package dev.skillforge.challenge.domain.model;

/** Résultat de l'application d'une décision : nouvel état et nœud suivant. */
public record DecisionOutcome(SimulationState state, ScenarioNode nextNode) {

    public boolean terminal() {
        return nextNode.terminal();
    }
}
