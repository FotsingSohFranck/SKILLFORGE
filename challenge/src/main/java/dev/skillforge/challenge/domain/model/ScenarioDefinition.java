package dev.skillforge.challenge.domain.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Définition complète et <strong>immuable</strong> d'un scénario de Final Challenge (le « pivot »).
 * C'est à la fois la commande d'import, le contenu stocké en {@code jsonb} dans une version, et le
 * modèle de lecture. Le moteur d'exécution (SF-92a-2) l'interprète ; SF-92a-1 se limite à la
 * valider et à la persister de façon versionnée.
 *
 * @param scenarioKey        clé stable lisible (slug), identifie le scénario entre versions
 * @param nodeId             nœud de fin auquel le challenge est rattaché (référence souple)
 * @param title              titre de la mission
 * @param objective          objectif général présenté à l'apprenant
 * @param briefing           briefing de mission (rôle, contexte, contraintes)
 * @param competencies       compétences évaluées (identifiants)
 * @param competencyWeights  pondération par compétence (somme = 100)
 * @param passThreshold      score minimal de validation (ex. 80)
 * @param renforceThreshold  score « à renforcer » (ex. 60), strictement inférieur au seuil de validation
 * @param initialNodeId      identifiant du premier nœud présenté
 * @param nodes              situations de la mission
 * @param criticalConditions conditions éliminatoires éventuelles
 * @param mode               mode d'évaluation (formatif/certificatif) ; {@code null} ⇒ {@link ChallengeMode#FORMATIF}
 */
public record ScenarioDefinition(
        String scenarioKey,
        UUID nodeId,
        String title,
        String objective,
        String briefing,
        List<String> competencies,
        Map<String, Integer> competencyWeights,
        int passThreshold,
        int renforceThreshold,
        String initialNodeId,
        List<ScenarioNode> nodes,
        List<CriticalCondition> criticalConditions,
        ChallengeMode mode) {

    /** Mode effectif : valeur par défaut {@link ChallengeMode#FORMATIF} si non précisé. */
    public ChallengeMode effectiveMode() {
        return mode == null ? ChallengeMode.FORMATIF : mode;
    }
}
