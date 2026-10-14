package dev.skillforge.catalogue.adapter.in.web.dto;

import java.util.UUID;

/** Représentation web d'un parcours (résumé). */
public record RoadmapSummaryDto(
        UUID id,
        String slug,
        String title,
        String summary,
        String kind,
        int position) {
}
