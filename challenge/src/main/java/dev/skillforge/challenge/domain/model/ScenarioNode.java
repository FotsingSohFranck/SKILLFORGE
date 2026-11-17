package dev.skillforge.challenge.domain.model;

import java.util.List;

/**
 * Situation/étape de la mission (cf. spec §6.2). Un nœud non terminal propose des {@link Choice}
 * et des {@link TransitionRule} ; un nœud terminal peut conclure la mission.
 */
public record ScenarioNode(
        String id,
        String title,
        String context,
        String event,
        InteractionType interactionType,
        List<String> competencies,
        List<Choice> choices,
        List<TransitionRule> transitions,
        boolean terminal) {
}
