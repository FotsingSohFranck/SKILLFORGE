package dev.skillforge.apprentissage.domain.model;

/**
 * Politique de validation d'un nœud (miroir de la colonne {@code nodes.completion_policy}).
 * Par défaut {@link #LESSONS_ONLY} : aucune régression du comportement historique.
 */
public enum CompletionPolicy {
    LESSONS_ONLY,
    LESSONS_AND_PRACTICAL;

    public static CompletionPolicy fromDb(String value) {
        if (value == null || value.isBlank()) {
            return LESSONS_ONLY;
        }
        try {
            return CompletionPolicy.valueOf(value);
        } catch (IllegalArgumentException e) {
            return LESSONS_ONLY;
        }
    }

    public boolean requiresPractical() {
        return this == LESSONS_AND_PRACTICAL;
    }
}
