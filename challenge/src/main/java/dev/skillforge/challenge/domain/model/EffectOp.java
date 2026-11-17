package dev.skillforge.challenge.domain.model;

/** Opération d'un {@link Effect} sur une variable de l'état de simulation. */
public enum EffectOp {
    /** Fixe la variable à la valeur. */
    SET,
    /** Augmente la variable de la valeur. */
    INCREASE,
    /** Diminue la variable de la valeur. */
    DECREASE
}
