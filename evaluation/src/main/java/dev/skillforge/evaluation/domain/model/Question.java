package dev.skillforge.evaluation.domain.model;

import java.util.List;

/** Question de quiz avec ses options et sa justification (révélée après correction). */
public record Question(
        QuestionId id,
        QuestionType type,
        String prompt,
        String justification,
        int position,
        List<Option> options) {

    /** Option correcte de la question (la première marquée correcte), ou {@code null}. */
    public Option correctOption() {
        return options.stream().filter(Option::correct).findFirst().orElse(null);
    }
}
