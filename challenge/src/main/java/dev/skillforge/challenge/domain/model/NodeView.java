package dev.skillforge.challenge.domain.model;

import java.util.List;

/**
 * Projection d'un nœud exposée à l'apprenant pendant la simulation. N'expose <strong>jamais</strong>
 * les scores, effets, transitions ni justifications (qui révéleraient la correction).
 */
public record NodeView(
        String id,
        String title,
        String context,
        String event,
        InteractionType interactionType,
        List<String> competencies,
        List<ChoiceView> choices) {

    public static NodeView of(ScenarioNode node) {
        return new NodeView(
                node.id(),
                node.title(),
                node.context(),
                node.event(),
                node.interactionType(),
                node.competencies() == null ? List.of() : node.competencies(),
                (node.choices() == null ? List.<Choice>of() : node.choices())
                        .stream().map(ChoiceView::of).toList());
    }
}
