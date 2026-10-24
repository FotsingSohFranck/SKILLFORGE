package dev.skillforge.lab.domain.port.in;

/**
 * Vue de l'épreuve pratique d'un nœud pour l'apprenant (lecture).
 *
 * @param exists        vrai si le nœud a une épreuve pratique
 * @param required      vrai si la politique du nœud exige la pratique pour le valider
 * @param title         intitulé (null si absente)
 * @param instructions  énoncé (null si absente)
 * @param latestStatus  statut de la dernière tentative (null si aucune)
 * @param latestPassed  vrai si la dernière tentative connue est réussie
 */
public record PracticalView(
        boolean exists,
        boolean required,
        String title,
        String instructions,
        String latestStatus,
        boolean latestPassed
) {
    public static PracticalView absent(boolean required) {
        return new PracticalView(false, required, null, null, null, false);
    }

    public static PracticalView present(boolean required, String title, String instructions,
                                        String latestStatus, boolean latestPassed) {
        return new PracticalView(true, required, title, instructions, latestStatus, latestPassed);
    }
}
