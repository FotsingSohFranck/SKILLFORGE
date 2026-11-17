package dev.skillforge.challenge.domain.model;

import java.util.List;

/**
 * Rapport final d'une tentative : score global pondéré [0,100], verdict (réussi / à renforcer /
 * éliminé), détail par compétence et conditions éliminatoires déclenchées.
 */
public record ChallengeResult(
        int globalScore,
        boolean passed,
        boolean reinforce,
        boolean eliminated,
        List<CompetencyScore> competencyScores,
        List<String> triggeredCritical) {
}
