package dev.skillforge.evaluation.domain.model;

import java.util.List;

/**
 * Défi de code : énoncé, squelette, solution attendue et <b>points clés</b> servant à la
 * validation (comparaison textuelle, sans exécution serveur).
 */
public record CodeChallenge(
        ChallengeId id,
        String title,
        String statement,
        String skeleton,
        String solution,
        List<String> keyPoints,
        String language) {
}
