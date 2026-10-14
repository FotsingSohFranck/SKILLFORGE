package dev.skillforge.evaluation.domain.model;

/** Option de réponse. {@code correct} n'est pas exposé au client avant correction. */
public record Option(
        OptionId id,
        String label,
        int position,
        boolean correct) {
}
