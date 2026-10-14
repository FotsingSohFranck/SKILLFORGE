package dev.skillforge.evaluation.domain.model;

import java.util.List;

/**
 * Item d'examen rattaché à un domaine et pondéré. Pour {@code MCQ}, {@code options} porte les
 * réponses (avec l'indicateur correct pour la notation, masqué à l'affichage) ;
 * pour {@code TASK}, {@code taskSolution} est la solution attendue (révélée après soumission).
 */
public record ExamItem(
        ExamItemId id,
        ExamDomainId domainId,
        ExamItemKind kind,
        String prompt,
        double points,
        int position,
        List<Option> options,
        String taskSolution) {

    public Option correctOption() {
        return options.stream().filter(Option::correct).findFirst().orElse(null);
    }
}
