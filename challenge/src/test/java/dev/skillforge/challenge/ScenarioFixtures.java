package dev.skillforge.challenge;

import dev.skillforge.challenge.domain.model.Choice;
import dev.skillforge.challenge.domain.model.Effect;
import dev.skillforge.challenge.domain.model.EffectOp;
import dev.skillforge.challenge.domain.model.InteractionType;
import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import dev.skillforge.challenge.domain.model.ScenarioNode;
import dev.skillforge.challenge.domain.model.TransitionRule;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Fabriques de scénarios pour les tests (valides et variantes à construire par les tests). */
public final class ScenarioFixtures {

    public static final UUID NODE = UUID.fromString("01920000-0000-7000-8000-00000000caca");

    private ScenarioFixtures() {
    }

    public static ScenarioNode choiceNode(String id, String nextId) {
        return new ScenarioNode(id, "T-" + id, "contexte", "événement",
                InteractionType.SINGLE_CHOICE, List.of("a"),
                List.of(new Choice("c1", "Choix", Map.of("a", 40),
                        List.of(new Effect("risk", EffectOp.INCREASE, 1)), "raison")),
                List.of(new TransitionRule("default", nextId)), false);
    }

    public static ScenarioNode terminal(String id) {
        return new ScenarioNode(id, "T-" + id, null, null, null,
                List.of(), List.of(), List.of(), true);
    }

    public static ScenarioDefinition def(String key, UUID node, List<String> comps,
                                         Map<String, Integer> weights, int pass, int renforce,
                                         String initial, List<ScenarioNode> nodes) {
        return new ScenarioDefinition(key, node, "Titre", "Objectif", "Briefing",
                comps, weights, pass, renforce, initial, nodes, List.of());
    }

    public static ScenarioDefinition valid(String key, UUID node) {
        return def(key, node, List.of("a", "b"), Map.of("a", 60, "b", 40), 80, 60, "n1",
                List.of(choiceNode("n1", "n2"), terminal("n2")));
    }

    public static ScenarioDefinition valid() {
        return valid("k", NODE);
    }
}
