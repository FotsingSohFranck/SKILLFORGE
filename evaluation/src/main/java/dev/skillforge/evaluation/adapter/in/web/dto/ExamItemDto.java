package dev.skillforge.evaluation.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

/** Item d'examen exposé pour le passage (sans bonne réponse ni solution de tâche). */
public record ExamItemDto(
        UUID id,
        UUID domainId,
        String kind,
        String prompt,
        double points,
        int position,
        List<OptionDto> options) {
}
