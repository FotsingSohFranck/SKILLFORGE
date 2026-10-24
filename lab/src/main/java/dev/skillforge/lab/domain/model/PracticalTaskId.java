package dev.skillforge.lab.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Identifiant d'une tâche pratique. */
public record PracticalTaskId(UUID value) {
    public PracticalTaskId {
        Objects.requireNonNull(value, "PracticalTaskId.value");
    }

    public static PracticalTaskId of(UUID value) {
        return new PracticalTaskId(value);
    }
}
