package dev.skillforge.lab.domain.model;

/** Cycle de vie d'une tentative d'épreuve pratique. */
public enum AttemptStatus {
    /** Créée, le conteneur n'a pas (encore) démarré. */
    PENDING,
    /** Environnement démarré, l'apprenant travaille. */
    RUNNING,
    /** Épreuve réussie (script de validation sorti avec le code 0). */
    PASSED,
    /** Épreuve échouée (script de validation sorti avec un code non nul). */
    FAILED,
    /** Session expirée avant validation (TTL dépassé). */
    EXPIRED;

    /** Un statut terminal ne peut plus changer. */
    public boolean isTerminal() {
        return this == PASSED || this == FAILED || this == EXPIRED;
    }
}
