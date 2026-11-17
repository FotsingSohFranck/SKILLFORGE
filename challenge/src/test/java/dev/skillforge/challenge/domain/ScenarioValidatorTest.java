package dev.skillforge.challenge.domain;

import dev.skillforge.challenge.ScenarioFixtures;
import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import dev.skillforge.challenge.domain.model.ScenarioNode;
import dev.skillforge.challenge.domain.model.TransitionRule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Validation structurelle d'une définition de scénario (pur, sans framework). */
class ScenarioValidatorTest {

    @Test
    void un_scenario_bien_forme_ne_produit_aucune_erreur() {
        assertTrue(ScenarioValidator.validate(ScenarioFixtures.valid()).isEmpty());
    }

    @Test
    void null_est_signale() {
        assertFalse(ScenarioValidator.validate(null).isEmpty());
    }

    @Test
    void somme_des_ponderations_differente_de_100_est_signalee() {
        ScenarioDefinition d = ScenarioFixtures.def("k", ScenarioFixtures.NODE,
                List.of("a", "b"), Map.of("a", 50, "b", 40), 80, 60, "n1",
                List.of(ScenarioFixtures.choiceNode("n1", "n2"), ScenarioFixtures.terminal("n2")));
        assertTrue(hasError(d, "pondérations"));
    }

    @Test
    void absence_de_noeud_terminal_est_signalee() {
        // n1 -> n2 -> n1 : aucun terminal.
        ScenarioNode n1 = ScenarioFixtures.choiceNode("n1", "n2");
        ScenarioNode n2 = ScenarioFixtures.choiceNode("n2", "n1");
        ScenarioDefinition d = ScenarioFixtures.def("k", ScenarioFixtures.NODE,
                List.of("a", "b"), Map.of("a", 60, "b", 40), 80, 60, "n1", List.of(n1, n2));
        assertTrue(hasError(d, "terminal"));
    }

    @Test
    void transition_vers_un_noeud_inexistant_est_signalee() {
        ScenarioNode n1 = ScenarioFixtures.choiceNode("n1", "fantome");
        ScenarioDefinition d = ScenarioFixtures.def("k", ScenarioFixtures.NODE,
                List.of("a", "b"), Map.of("a", 60, "b", 40), 80, 60, "n1",
                List.of(n1, ScenarioFixtures.terminal("n2")));
        assertTrue(hasError(d, "inexistant"));
    }

    @Test
    void noeud_inatteignable_est_signale() {
        ScenarioDefinition d = ScenarioFixtures.def("k", ScenarioFixtures.NODE,
                List.of("a", "b"), Map.of("a", 60, "b", 40), 80, 60, "n1",
                List.of(ScenarioFixtures.choiceNode("n1", "n2"),
                        ScenarioFixtures.terminal("n2"),
                        ScenarioFixtures.terminal("orphelin")));
        assertTrue(hasError(d, "inatteignable"));
    }

    @Test
    void seuils_incoherents_sont_signales() {
        ScenarioDefinition d = ScenarioFixtures.def("k", ScenarioFixtures.NODE,
                List.of("a", "b"), Map.of("a", 60, "b", 40), 80, 80, "n1",
                List.of(ScenarioFixtures.choiceNode("n1", "n2"), ScenarioFixtures.terminal("n2")));
        assertTrue(hasError(d, "Seuils"));
    }

    @Test
    void noeud_non_terminal_sans_transition_est_signale() {
        ScenarioNode isole = new ScenarioNode("n1", "T", "c", "e",
                dev.skillforge.challenge.domain.model.InteractionType.SINGLE_CHOICE,
                List.of("a"),
                List.of(new dev.skillforge.challenge.domain.model.Choice("c1", "L", Map.of("a", 1),
                        List.of(), "r")),
                List.<TransitionRule>of(), false);
        ScenarioDefinition d = ScenarioFixtures.def("k", ScenarioFixtures.NODE,
                List.of("a", "b"), Map.of("a", 60, "b", 40), 80, 60, "n1",
                List.of(isole, ScenarioFixtures.terminal("n2")));
        assertTrue(hasError(d, "transition"));
    }

    private static boolean hasError(ScenarioDefinition d, String fragment) {
        return ScenarioValidator.validate(d).stream().anyMatch(e -> e.contains(fragment));
    }
}
