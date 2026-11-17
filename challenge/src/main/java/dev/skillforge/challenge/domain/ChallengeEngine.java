package dev.skillforge.challenge.domain;

import dev.skillforge.challenge.domain.model.ChallengeResult;
import dev.skillforge.challenge.domain.model.Choice;
import dev.skillforge.challenge.domain.model.CompetencyScore;
import dev.skillforge.challenge.domain.model.CriticalCondition;
import dev.skillforge.challenge.domain.model.Decision;
import dev.skillforge.challenge.domain.model.DecisionOutcome;
import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import dev.skillforge.challenge.domain.model.ScenarioNode;
import dev.skillforge.challenge.domain.model.SimulationState;
import dev.skillforge.challenge.domain.model.TransitionRule;
import dev.skillforge.common.error.BusinessRuleException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Moteur de simulation <strong>déterministe</strong> et pur (aucune dépendance framework) :
 * même scénario + même état + même décision ⇒ même résultat.
 *
 * <ul>
 *   <li>{@link #decide} applique les effets du choix, puis choisit la transition : première dont la
 *       condition {@code whenState} est vraie (ordre de déclaration), sinon la dernière (repli).</li>
 *   <li>{@link #score} calcule, par compétence, le pourcentage = points obtenus / points maximum
 *       atteignables sur le chemin parcouru (meilleur choix de chaque nœud visité), puis la moyenne
 *       pondérée des compétences exercées. Les conditions éliminatoires sont évaluées sur l'état final.</li>
 * </ul>
 */
public final class ChallengeEngine {

    private ChallengeEngine() {
    }

    public static ScenarioNode nodeById(ScenarioDefinition def, String nodeId) {
        return def.nodes().stream()
                .filter(n -> n.id().equals(nodeId))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("UNKNOWN_NODE", "Nœud inconnu : " + nodeId));
    }

    public static ScenarioNode initialNode(ScenarioDefinition def) {
        return nodeById(def, def.initialNodeId());
    }

    public static DecisionOutcome decide(ScenarioDefinition def, SimulationState state,
                                         String currentNodeId, String choiceId) {
        ScenarioNode node = nodeById(def, currentNodeId);
        if (node.terminal()) {
            throw new BusinessRuleException("ATTEMPT_COMPLETED", "La simulation est déjà terminée.");
        }
        Choice choice = node.choices().stream()
                .filter(c -> c.id().equals(choiceId))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException(
                        "INVALID_CHOICE", "Choix invalide pour ce nœud : " + choiceId));

        SimulationState next = state.apply(choice.effects());
        ScenarioNode nextNode = nodeById(def, nextNodeId(node, next));
        return new DecisionOutcome(next, nextNode);
    }

    private static String nextNodeId(ScenarioNode node, SimulationState state) {
        List<TransitionRule> transitions = node.transitions();
        for (TransitionRule t : transitions) {
            if (StateExpression.matches(t.whenState(), state)) {
                return t.nextNodeId();
            }
        }
        // Repli : aucune condition vraie ⇒ dernière transition déclarée.
        return transitions.get(transitions.size() - 1).nextNodeId();
    }

    public static ChallengeResult score(ScenarioDefinition def, List<Decision> decisions,
                                        SimulationState finalState) {
        Map<String, Integer> raw = new LinkedHashMap<>();
        Map<String, Integer> max = new LinkedHashMap<>();
        for (String competency : def.competencies()) {
            raw.put(competency, 0);
            max.put(competency, 0);
        }

        for (Decision decision : decisions) {
            ScenarioNode node = nodeById(def, decision.nodeId());
            Choice chosen = node.choices().stream()
                    .filter(c -> c.id().equals(decision.choiceId()))
                    .findFirst()
                    .orElse(null);
            if (chosen == null) {
                continue;
            }
            for (String competency : def.competencies()) {
                raw.merge(competency, scoreOf(chosen, competency), Integer::sum);
                int best = node.choices().stream()
                        .mapToInt(c -> Math.max(0, scoreOf(c, competency)))
                        .max().orElse(0);
                if (best > 0) {
                    max.merge(competency, best, Integer::sum);
                }
            }
        }

        List<CompetencyScore> competencyScores = new ArrayList<>();
        long weightedSum = 0;
        long weightTotal = 0;
        for (String competency : def.competencies()) {
            int m = max.get(competency);
            int r = raw.get(competency);
            int percent = m <= 0 ? 0 : clamp(Math.round(r * 100f / m));
            competencyScores.add(new CompetencyScore(competency, r, m, percent));
            if (m > 0) {
                int weight = def.competencyWeights().getOrDefault(competency, 0);
                weightedSum += (long) weight * percent;
                weightTotal += weight;
            }
        }
        int global = weightTotal <= 0 ? 0 : Math.round((float) weightedSum / weightTotal);

        List<String> triggered = new ArrayList<>();
        if (def.criticalConditions() != null) {
            for (CriticalCondition condition : def.criticalConditions()) {
                if (StateExpression.isConditional(condition.whenState())
                        && StateExpression.matches(condition.whenState(), finalState)) {
                    triggered.add(condition.id());
                }
            }
        }

        boolean eliminated = !triggered.isEmpty();
        boolean passed = !eliminated && global >= def.passThreshold();
        boolean reinforce = !passed && !eliminated && global >= def.renforceThreshold();
        return new ChallengeResult(global, passed, reinforce, eliminated, competencyScores, triggered);
    }

    private static int scoreOf(Choice choice, String competency) {
        Map<String, Integer> scores = choice.competencyScores();
        return scores == null ? 0 : scores.getOrDefault(competency, 0);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
