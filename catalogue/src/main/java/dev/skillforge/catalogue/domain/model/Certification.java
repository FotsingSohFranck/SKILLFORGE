package dev.skillforge.catalogue.domain.model;

import java.util.List;
import java.util.UUID;

/** Certification liée à un parcours (code, format d'examen, durée) + ses domaines pondérés. */
public record Certification(
        UUID id,
        UUID roadmapId,
        String code,
        String examFormat,
        int durationMinutes,
        Double passingScore,
        List<ExamDomain> domains) {
}
