package dev.skillforge.challenge.domain;

import dev.skillforge.challenge.domain.model.SimulationState;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Évaluateur d'expressions d'état : opérateurs, repli par défaut, robustesse. */
class StateExpressionTest {

    private final SimulationState state = new SimulationState(Map.of("danger", 5, "score", 0));

    @Test
    void operateurs_de_comparaison() {
        assertTrue(StateExpression.matches("danger >= 5", state));
        assertTrue(StateExpression.matches("danger > 4", state));
        assertFalse(StateExpression.matches("danger > 5", state));
        assertTrue(StateExpression.matches("danger == 5", state));
        assertTrue(StateExpression.matches("danger != 0", state));
        assertTrue(StateExpression.matches("score <= 0", state));
        assertFalse(StateExpression.matches("score < 0", state));
    }

    @Test
    void variable_absente_vaut_zero() {
        assertTrue(StateExpression.matches("inconnue == 0", state));
        assertFalse(StateExpression.matches("inconnue > 0", state));
    }

    @Test
    void vide_et_default_sont_le_repli_vrai() {
        assertTrue(StateExpression.matches("default", state));
        assertTrue(StateExpression.matches("", state));
        assertTrue(StateExpression.matches(null, state));
    }

    @Test
    void expression_non_analysable_ne_matche_pas() {
        assertFalse(StateExpression.matches("danger >= abc", state));
        assertFalse(StateExpression.matches("n'importe quoi", state));
    }

    @Test
    void isConditional_distingue_le_repli() {
        assertTrue(StateExpression.isConditional("danger >= 5"));
        assertFalse(StateExpression.isConditional("default"));
        assertFalse(StateExpression.isConditional(""));
        assertFalse(StateExpression.isConditional(null));
    }
}
