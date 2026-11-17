package dev.skillforge.challenge.domain.model;

/**
 * Type d'interaction d'un nœud de simulation. v1 : uniquement des formats <strong>discrets
 * auto-notables</strong> (cf. ADR-0009 / SF-92a). Les réponses libres / commandes viendront
 * plus tard (dépendent d'un grader ou d'une validation humaine).
 */
public enum InteractionType {
    /** Choix d'une seule option parmi plusieurs. */
    SINGLE_CHOICE,
    /** Sélection de plusieurs options. */
    MULTI_CHOICE,
    /** Classement d'options dans le bon ordre. */
    ORDERING,
    /** Diagnostic : identifier la cause à partir d'informations. */
    DIAGNOSIS
}
