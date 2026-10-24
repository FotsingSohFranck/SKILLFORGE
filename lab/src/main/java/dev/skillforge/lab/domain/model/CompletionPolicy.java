package dev.skillforge.lab.domain.model;

/**
 * Politique de validation d'un nœud.
 *
 * <ul>
 *   <li>{@link #LESSONS_ONLY} : comportement historique (nœud validé quand toutes les leçons
 *       sont terminées). Valeur par défaut, aucune régression.</li>
 *   <li>{@link #LESSONS_AND_PRACTICAL} : le nœud n'est validé que si les leçons sont terminées
 *       <em>et</em> l'épreuve pratique est réussie.</li>
 * </ul>
 */
public enum CompletionPolicy {
    LESSONS_ONLY,
    LESSONS_AND_PRACTICAL
}
