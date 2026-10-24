package dev.skillforge.lab.domain.port.in;

import java.util.UUID;

/**
 * Commande d'import d'une épreuve pratique pour un nœud (pipeline de contenu / authoring).
 *
 * @param nodeId           nœud concerné
 * @param title            intitulé de l'épreuve
 * @param image            image de lab (par famille de parcours), ex. {@code skillforge-lab-backend:pilot}
 * @param instructions     énoncé affiché à l'apprenant
 * @param workspaceSeed    contenu initial du workspace (peut être {@code null})
 * @param validationRef    commande de validation exécutée dans le conteneur (exit 0 = réussi)
 * @param timeoutSeconds   TTL de la session, en secondes (> 0)
 * @param requiresPractical vrai si l'épreuve est requise pour valider le nœud (politique)
 */
public record PracticalTaskImport(
        UUID nodeId,
        String title,
        String image,
        String instructions,
        String workspaceSeed,
        String validationRef,
        int timeoutSeconds,
        boolean requiresPractical
) {
}
