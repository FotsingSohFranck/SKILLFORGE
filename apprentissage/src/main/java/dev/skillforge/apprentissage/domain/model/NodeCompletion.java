package dev.skillforge.apprentissage.domain.model;

/**
 * Règle (pure) de validation d'un nœud : toutes les leçons terminées, et chaque exigence activée
 * satisfaite (épreuve pratique, Final Challenge). Centralise la condition utilisée aux différents
 * points de recalcul pour garantir un gating cohérent.
 */
public final class NodeCompletion {

    private NodeCompletion() {
    }

    public static boolean isComplete(long lessonsDone, long lessonsTotal,
                                     boolean requiresPractical, boolean practicalPassed,
                                     boolean requiresChallenge, boolean challengePassed) {
        boolean lessonsOk = lessonsTotal > 0 && lessonsDone >= lessonsTotal;
        boolean practicalOk = !requiresPractical || practicalPassed;
        boolean challengeOk = !requiresChallenge || challengePassed;
        return lessonsOk && practicalOk && challengeOk;
    }
}
