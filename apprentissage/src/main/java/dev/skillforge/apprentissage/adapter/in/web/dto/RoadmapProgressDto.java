package dev.skillforge.apprentissage.adapter.in.web.dto;

import java.util.List;

/** Représentation web de la progression d'un parcours. */
public record RoadmapProgressDto(
        EnrollmentDto enrollment,
        List<NodeProgressDto> nodes) {
}
