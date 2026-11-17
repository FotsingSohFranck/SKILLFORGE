package dev.skillforge.challenge.domain.model;

import java.util.UUID;

/**
 * Vue d'une tentative renvoyée à l'apprenant : le briefing/objectif du scénario, le nœud courant
 * (si en cours) ou le rapport final (si terminée).
 */
public record AttemptView(
        UUID attemptId,
        AttemptStatus status,
        String briefing,
        String objective,
        NodeView node,
        ChallengeResult result) {

    public static AttemptView inProgress(UUID attemptId, ScenarioDefinition def, ScenarioNode node) {
        return new AttemptView(attemptId, AttemptStatus.IN_PROGRESS,
                def.briefing(), def.objective(), NodeView.of(node), null);
    }

    public static AttemptView completed(UUID attemptId, ScenarioDefinition def, ChallengeResult result) {
        return new AttemptView(attemptId, AttemptStatus.COMPLETED,
                def.briefing(), def.objective(), null, result);
    }
}
