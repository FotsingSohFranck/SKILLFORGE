package dev.skillforge.challenge.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * Tentative d'un apprenant sur un scénario de Final Challenge. Le scénario est <strong>figé</strong>
 * à une version précise ({@link ScenarioRef}) pour garantir l'immuabilité des tentatives passées.
 * {@code result} n'est renseigné qu'une fois la tentative terminée.
 */
public record ChallengeAttempt(
        UUID id,
        ScenarioRef scenario,
        UUID nodeId,
        UUID learnerId,
        AttemptStatus status,
        String currentNodeId,
        SimulationState state,
        List<Decision> decisions,
        ChallengeResult result) {
}
