package dev.skillforge.lab.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Identifiant d'un apprenant (référence souple vers iam, propre au contexte lab). */
public record LearnerId(UUID value) {
    public LearnerId {
        Objects.requireNonNull(value, "LearnerId.value");
    }

    public static LearnerId of(UUID value) {
        return new LearnerId(value);
    }
}
