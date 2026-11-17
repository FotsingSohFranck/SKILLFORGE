package dev.skillforge.challenge.domain;

import dev.skillforge.challenge.domain.model.ChallengeResult;
import dev.skillforge.challenge.domain.model.Choice;
import dev.skillforge.challenge.domain.model.CriticalCondition;
import dev.skillforge.challenge.domain.model.Decision;
import dev.skillforge.challenge.domain.model.DecisionOutcome;
import dev.skillforge.challenge.domain.model.Effect;
import dev.skillforge.challenge.domain.model.EffectOp;
import dev.skillforge.challenge.domain.model.GuidanceLevel;
import dev.skillforge.challenge.domain.model.InteractionType;
import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import dev.skillforge.challenge.domain.model.ScenarioNode;
import dev.skillforge.challenge.domain.model.SimulationState;
import dev.skillforge.challenge.domain.model.TransitionRule;
import dev.skillforge.common.error.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Moteur déterministe : transitions conditionnelles, effets, scoring, conditions éliminatoires. */
class ChallengeEngineTest {

    private final ScenarioDefinition scenario = scenario();

    @Test
    void initial_node_est_le_point_de_depart() {
        assertEquals("n1", ChallengeEngine.initialNode(scenario).id());
    }

    @Test
    void choix_sur_de_mene_a_la_bonne_fin_sans_danger() {
        DecisionOutcome out = ChallengeEngine.decide(scenario, SimulationState.empty(), "n1", "good");
        assertEquals(0, out.state().get("danger"));
        assertEquals("good-end", out.nextNode().id());
        assertTrue(out.terminal());
    }

    @Test
    void choix_risque_declenche_la_transition_conditionnelle() {
        DecisionOutcome out = ChallengeEngine.decide(scenario, SimulationState.empty(), "n1", "risky");
        assertEquals(5, out.state().get("danger"));
        assertEquals("bad-end", out.nextNode().id());
    }

    @Test
    void chemin_sur_est_reussi_a_100() {
        DecisionOutcome out = ChallengeEngine.decide(scenario, SimulationState.empty(), "n1", "good");
        ChallengeResult result = ChallengeEngine.score(
                scenario, List.of(new Decision(1, "n1", "good")), out.state());

        assertEquals(100, result.globalScore());
        assertTrue(result.passed());
        assertFalse(result.eliminated());
        assertFalse(result.reinforce());
        assertEquals(1, result.competencyScores().size());
        assertEquals(100, result.competencyScores().get(0).percent());
    }

    @Test
    void chemin_risque_est_eliminatoire_malgre_un_score_partiel() {
        DecisionOutcome out = ChallengeEngine.decide(scenario, SimulationState.empty(), "n1", "risky");
        ChallengeResult result = ChallengeEngine.score(
                scenario, List.of(new Decision(1, "n1", "risky")), out.state());

        assertEquals(50, result.globalScore()); // 5 / 10 points de la compétence « a »
        assertTrue(result.eliminated());
        assertFalse(result.passed());
        assertFalse(result.reinforce());
        assertEquals(List.of("crit"), result.triggeredCritical());
    }

    @Test
    void guidage_absent_avant_toute_decision() {
        assertNull(ChallengeEngine.guidance(scenario, List.of(), SimulationState.empty()));
    }

    @Test
    void guidage_favorable_sur_un_bon_choix() {
        DecisionOutcome out = ChallengeEngine.decide(scenario, SimulationState.empty(), "n1", "good");
        assertEquals(GuidanceLevel.FAVORABLE,
                ChallengeEngine.guidance(scenario, List.of(new Decision(1, "n1", "good")), out.state()));
    }

    @Test
    void guidage_a_surveiller_sur_un_choix_moyen() {
        DecisionOutcome out = ChallengeEngine.decide(scenario, SimulationState.empty(), "n1", "medium");
        assertEquals(GuidanceLevel.A_SURVEILLER,
                ChallengeEngine.guidance(scenario, List.of(new Decision(1, "n1", "medium")), out.state()));
    }

    @Test
    void guidage_defavorable_sur_une_condition_eliminatoire() {
        DecisionOutcome out = ChallengeEngine.decide(scenario, SimulationState.empty(), "n1", "risky");
        assertEquals(GuidanceLevel.DEFAVORABLE,
                ChallengeEngine.guidance(scenario, List.of(new Decision(1, "n1", "risky")), out.state()));
    }

    @Test
    void choix_inexistant_est_rejete() {
        assertThrows(BusinessRuleException.class,
                () -> ChallengeEngine.decide(scenario, SimulationState.empty(), "n1", "inconnu"));
    }

    @Test
    void decider_sur_un_noeud_terminal_est_rejete() {
        assertThrows(BusinessRuleException.class,
                () -> ChallengeEngine.decide(scenario, SimulationState.empty(), "good-end", "good"));
    }

    private static ScenarioDefinition scenario() {
        Choice good = new Choice("good", "Choix sûr", Map.of("a", 10), List.of(), "sûr");
        Choice medium = new Choice("medium", "Choix moyen", Map.of("a", 7), List.of(), "moyen");
        Choice risky = new Choice("risky", "Choix risqué", Map.of("a", 5),
                List.of(new Effect("danger", EffectOp.SET, 5)), "risqué");
        ScenarioNode n1 = new ScenarioNode("n1", "Décision", "contexte", "événement",
                InteractionType.SINGLE_CHOICE, List.of("a"), List.of(good, medium, risky),
                List.of(new TransitionRule("danger >= 5", "bad-end"),
                        new TransitionRule("default", "good-end")), false);
        ScenarioNode goodEnd = terminal("good-end");
        ScenarioNode badEnd = terminal("bad-end");
        CriticalCondition crit = new CriticalCondition("crit", "Danger critique atteint", "danger >= 5");
        return new ScenarioDefinition("k", UUID.randomUUID(), "Titre", "Objectif", "Briefing",
                List.of("a"), Map.of("a", 100), 80, 50, "n1",
                List.of(n1, goodEnd, badEnd), List.of(crit), null);
    }

    private static ScenarioNode terminal(String id) {
        return new ScenarioNode(id, "T-" + id, null, null, null, List.of(), List.of(), List.of(), true);
    }
}
