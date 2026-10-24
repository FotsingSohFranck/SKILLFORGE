package dev.skillforge.lab.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Définition d'une épreuve pratique rattachée à un nœud.
 *
 * <p>Value object immuable du domaine : l'orchestration (démarrage du conteneur, exécution du
 * script de validation) est hors domaine et interviendra via des ports dans SF-91b et suivantes.
 *
 * @param id             identifiant de la tâche
 * @param nodeId         nœud concerné (référence souple vers le catalogue)
 * @param title          intitulé de l'épreuve
 * @param image          image de base du conteneur (par famille de parcours)
 * @param instructions   énoncé affiché à l'apprenant
 * @param workspaceSeed  contenu initial du dossier de travail (peut être {@code null})
 * @param validationRef  référence du script de validation (code de sortie 0 = réussi)
 * @param timeoutSeconds TTL de la session, en secondes (> 0)
 */
public record PracticalTask(
        PracticalTaskId id,
        UUID nodeId,
        String title,
        String image,
        String instructions,
        String workspaceSeed,
        String validationRef,
        int timeoutSeconds
) {
    public PracticalTask {
        Objects.requireNonNull(id, "PracticalTask.id");
        Objects.requireNonNull(nodeId, "PracticalTask.nodeId");
        requireText(title, "title");
        requireText(image, "image");
        requireText(instructions, "instructions");
        requireText(validationRef, "validationRef");
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("PracticalTask.timeoutSeconds doit etre > 0");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PracticalTask." + field + " est requis");
        }
    }
}
