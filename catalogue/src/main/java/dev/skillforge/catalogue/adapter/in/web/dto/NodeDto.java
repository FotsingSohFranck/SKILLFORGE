package dev.skillforge.catalogue.adapter.in.web.dto;

import java.util.UUID;

/** Représentation web d'un nœud. */
public record NodeDto(
        UUID id,
        UUID roadmapId,
        UUID phaseId,
        String slug,
        String title,
        String summary,
        String importance,
        Integer estimatedMinutes,
        int position) {
}
