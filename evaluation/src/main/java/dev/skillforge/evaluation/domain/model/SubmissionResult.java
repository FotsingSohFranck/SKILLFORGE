package dev.skillforge.evaluation.domain.model;

import java.util.List;

/**
 * Résultat de validation d'une soumission : réussite, points clés couverts / manquants,
 * et solution attendue (révélée après soumission).
 */
public record SubmissionResult(
        boolean passed,
        List<String> matched,
        List<String> missing,
        String solution) {
}
