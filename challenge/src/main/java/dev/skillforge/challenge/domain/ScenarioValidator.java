package dev.skillforge.challenge.domain;

import dev.skillforge.challenge.domain.model.Choice;
import dev.skillforge.challenge.domain.model.Effect;
import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import dev.skillforge.challenge.domain.model.ScenarioNode;
import dev.skillforge.challenge.domain.model.TransitionRule;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validation structurelle d'une définition de scénario avant publication (spec §14.4). Pur, sans
 * framework : renvoie la liste des erreurs (vide = valide), afin que la couche applicative décide
 * de la traduction en exception/HTTP et que le validateur reste testable unitairement.
 *
 * <p>Couvre en SF-92a-1 : champs requis, cohérence des compétences/pondérations, seuils, unicité
 * des identifiants, intégrité du graphe (transitions vers des nœuds existants, présence et
 * atteignabilité d'un nœud terminal, pas de nœud inatteignable). Les règles sémantiques plus fines
 * (contradictions de conditions, couverture des compétences obligatoires par branche) relèveront de
 * l'outil d'authoring (SF-92/§14.4) et du moteur (SF-92a-2).
 */
public final class ScenarioValidator {

    private ScenarioValidator() {
    }

    public static List<String> validate(ScenarioDefinition d) {
        List<String> errors = new ArrayList<>();
        if (d == null) {
            errors.add("La définition du scénario est requise.");
            return errors;
        }

        if (isBlank(d.scenarioKey())) {
            errors.add("La clé du scénario (scenarioKey) est requise.");
        }
        if (d.nodeId() == null) {
            errors.add("Le nœud rattaché (nodeId) est requis.");
        }
        if (isBlank(d.title())) {
            errors.add("Le titre est requis.");
        }
        if (isBlank(d.objective())) {
            errors.add("L'objectif est requis.");
        }

        Set<String> competencies = validateCompetencies(d, errors);
        validateWeights(d, competencies, errors);
        validateThresholds(d, errors);
        validateGraph(d, competencies, errors);

        return errors;
    }

    private static Set<String> validateCompetencies(ScenarioDefinition d, List<String> errors) {
        Set<String> set = new HashSet<>();
        List<String> competencies = nullToEmpty(d.competencies());
        if (competencies.isEmpty()) {
            errors.add("Au moins une compétence évaluée est requise.");
            return set;
        }
        for (String c : competencies) {
            if (isBlank(c)) {
                errors.add("Une compétence est vide.");
            } else if (!set.add(c)) {
                errors.add("Compétence en double : " + c + ".");
            }
        }
        return set;
    }

    private static void validateWeights(ScenarioDefinition d, Set<String> competencies, List<String> errors) {
        Map<String, Integer> weights = d.competencyWeights();
        if (weights == null || weights.isEmpty()) {
            errors.add("Les pondérations par compétence (competencyWeights) sont requises.");
            return;
        }
        int sum = 0;
        for (Map.Entry<String, Integer> e : weights.entrySet()) {
            if (!competencies.contains(e.getKey())) {
                errors.add("Pondération pour une compétence inconnue : " + e.getKey() + ".");
            }
            if (e.getValue() == null || e.getValue() <= 0) {
                errors.add("Pondération invalide (doit être > 0) pour : " + e.getKey() + ".");
            } else {
                sum += e.getValue();
            }
        }
        for (String c : competencies) {
            if (!weights.containsKey(c)) {
                errors.add("Pondération manquante pour la compétence : " + c + ".");
            }
        }
        if (sum != 100) {
            errors.add("La somme des pondérations doit être 100 (actuelle : " + sum + ").");
        }
    }

    private static void validateThresholds(ScenarioDefinition d, List<String> errors) {
        if (!(d.renforceThreshold() > 0
                && d.renforceThreshold() < d.passThreshold()
                && d.passThreshold() <= 100)) {
            errors.add("Seuils invalides : attendu 0 < renforceThreshold < passThreshold <= 100 "
                    + "(reçu renforceThreshold=" + d.renforceThreshold()
                    + ", passThreshold=" + d.passThreshold() + ").");
        }
    }

    private static void validateGraph(ScenarioDefinition d, Set<String> competencies, List<String> errors) {
        List<ScenarioNode> nodes = nullToEmpty(d.nodes());
        if (nodes.isEmpty()) {
            errors.add("Au moins un nœud est requis.");
            return;
        }

        Set<String> ids = new HashSet<>();
        for (ScenarioNode n : nodes) {
            if (isBlank(n.id())) {
                errors.add("Un nœud a un identifiant vide.");
            } else if (!ids.add(n.id())) {
                errors.add("Identifiant de nœud en double : " + n.id() + ".");
            }
        }

        if (isBlank(d.initialNodeId()) || !ids.contains(d.initialNodeId())) {
            errors.add("Le nœud initial (initialNodeId) doit référencer un nœud existant.");
        }

        boolean anyTerminal = false;
        for (ScenarioNode n : nodes) {
            if (n.terminal()) {
                anyTerminal = true;
            }
            validateNode(n, ids, competencies, errors);
        }
        if (!anyTerminal) {
            errors.add("Le scénario doit comporter au moins un nœud terminal.");
        }

        validateReachability(d, nodes, ids, errors);
    }

    private static void validateNode(ScenarioNode n, Set<String> nodeIds, Set<String> competencies,
                                     List<String> errors) {
        String label = isBlank(n.id()) ? "<sans id>" : n.id();
        if (n.interactionType() == null && !n.terminal()) {
            errors.add("Nœud " + label + " : interactionType requis (nœud non terminal).");
        }
        for (String c : nullToEmpty(n.competencies())) {
            if (!competencies.contains(c)) {
                errors.add("Nœud " + label + " : compétence inconnue « " + c + " ».");
            }
        }

        List<Choice> choices = nullToEmpty(n.choices());
        List<TransitionRule> transitions = nullToEmpty(n.transitions());
        if (!n.terminal()) {
            if (choices.isEmpty()) {
                errors.add("Nœud " + label + " : au moins un choix est requis (nœud non terminal).");
            }
            if (transitions.isEmpty()) {
                errors.add("Nœud " + label + " : au moins une transition est requise (nœud non terminal).");
            }
        }

        Set<String> choiceIds = new HashSet<>();
        for (Choice ch : choices) {
            if (isBlank(ch.id())) {
                errors.add("Nœud " + label + " : un choix a un identifiant vide.");
            } else if (!choiceIds.add(ch.id())) {
                errors.add("Nœud " + label + " : identifiant de choix en double « " + ch.id() + " ».");
            }
            if (isBlank(ch.label())) {
                errors.add("Nœud " + label + " : le choix « " + ch.id() + " » n'a pas de libellé.");
            }
            validateScores(ch, label, competencies, errors);
            for (Effect ef : nullToEmpty(ch.effects())) {
                if (isBlank(ef.variable()) || ef.op() == null) {
                    errors.add("Nœud " + label + " : effet invalide dans le choix « " + ch.id() + " ».");
                }
            }
        }

        for (TransitionRule t : transitions) {
            if (isBlank(t.nextNodeId()) || !nodeIds.contains(t.nextNodeId())) {
                errors.add("Nœud " + label + " : transition vers un nœud inexistant « "
                        + t.nextNodeId() + " ».");
            }
        }
    }

    private static void validateScores(Choice ch, String nodeLabel, Set<String> competencies,
                                       List<String> errors) {
        if (ch.competencyScores() == null) {
            return;
        }
        for (String c : ch.competencyScores().keySet()) {
            if (!competencies.contains(c)) {
                errors.add("Nœud " + nodeLabel + " : score pour une compétence inconnue « " + c
                        + " » dans le choix « " + ch.id() + " ».");
            }
        }
    }

    /** BFS depuis le nœud initial : tous les nœuds doivent être atteignables et mener à un terminal. */
    private static void validateReachability(ScenarioDefinition d, List<ScenarioNode> nodes,
                                             Set<String> ids, List<String> errors) {
        if (isBlank(d.initialNodeId()) || !ids.contains(d.initialNodeId())) {
            return; // déjà signalé
        }
        Map<String, ScenarioNode> byId = new HashSet<>(nodes).stream()
                .collect(java.util.stream.Collectors.toMap(ScenarioNode::id, n -> n, (a, b) -> a));

        Set<String> reachable = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(d.initialNodeId());
        reachable.add(d.initialNodeId());
        boolean reachesTerminal = false;
        while (!queue.isEmpty()) {
            ScenarioNode n = byId.get(queue.poll());
            if (n == null) {
                continue;
            }
            if (n.terminal()) {
                reachesTerminal = true;
            }
            for (TransitionRule t : nullToEmpty(n.transitions())) {
                if (t.nextNodeId() != null && ids.contains(t.nextNodeId()) && reachable.add(t.nextNodeId())) {
                    queue.add(t.nextNodeId());
                }
            }
        }
        if (!reachesTerminal) {
            errors.add("Aucun nœud terminal n'est atteignable depuis le nœud initial.");
        }
        for (ScenarioNode n : nodes) {
            if (!reachable.contains(n.id())) {
                errors.add("Nœud inatteignable depuis le nœud initial : " + n.id() + ".");
            }
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }
}
