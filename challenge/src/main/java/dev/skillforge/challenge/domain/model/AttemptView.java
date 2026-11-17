package dev.skillforge.challenge.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * Vue d'une tentative renvoyée à l'apprenant : le briefing/objectif du scénario, le nœud courant
 * (si en cours) ou le rapport final (si terminée), et l'historique (révision) des décisions prises.
 */
public record AttemptView(
        UUID attemptId,
        AttemptStatus status,
        String briefing,
        String objective,
        NodeView node,
        GuidanceLevel guidance,
        List<DecisionReview> history,
        ChallengeResult result) {

    public static AttemptView inProgress(UUID attemptId, ScenarioDefinition def, ScenarioNode node,
                                         GuidanceLevel guidance, List<DecisionReview> history) {
        return new AttemptView(attemptId, AttemptStatus.IN_PROGRESS,
                def.briefing(), def.objective(), NodeView.of(node), guidance, history, null);
    }

    public static AttemptView completed(UUID attemptId, ScenarioDefinition def, ChallengeResult result,
                                        List<DecisionReview> history) {
        return new AttemptView(attemptId, AttemptStatus.COMPLETED,
                def.briefing(), def.objective(), null, null, history, result);
    }
}
