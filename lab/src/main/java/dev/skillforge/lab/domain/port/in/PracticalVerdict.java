package dev.skillforge.lab.domain.port.in;

import java.util.UUID;

/**
 * Résultat rendu à l'apprenant après une soumission.
 *
 * @param attemptId identifiant de la tentative persistée
 * @param passed    vrai si l'épreuve est réussie
 * @param output    sortie (filtrée) du script de validation, pour le feedback
 */
public record PracticalVerdict(UUID attemptId, boolean passed, String output) {
}
