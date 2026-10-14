package dev.skillforge.evaluation.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

/** Résultat d'une tentative d'examen (score par domaine + points faibles). */
public record ExamResultDto(
        UUID attemptId,
        double totalScore,
        double maxScore,
        boolean passed,
        boolean expired,
        List<DomainResultDto> domains) {
}
