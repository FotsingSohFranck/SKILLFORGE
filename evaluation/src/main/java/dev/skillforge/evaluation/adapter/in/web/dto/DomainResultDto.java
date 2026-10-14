package dev.skillforge.evaluation.adapter.in.web.dto;

import java.util.UUID;

/** Résultat d'un domaine d'examen. */
public record DomainResultDto(
        UUID domainId,
        double score,
        double maxScore,
        boolean weak) {
}
