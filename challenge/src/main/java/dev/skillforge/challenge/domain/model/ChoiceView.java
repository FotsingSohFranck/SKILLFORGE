package dev.skillforge.challenge.domain.model;

/** Projection d'un choix exposée à l'apprenant : identifiant + libellé uniquement (pas de score). */
public record ChoiceView(String id, String label) {

    public static ChoiceView of(Choice choice) {
        return new ChoiceView(choice.id(), choice.label());
    }
}
