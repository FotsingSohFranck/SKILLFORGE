package dev.skillforge.catalogue.adapter.in.web.dto;

import java.util.List;

/** Représentation web d'un parcours avec son graphe. */
public record RoadmapGraphDto(
        RoadmapSummaryDto roadmap,
        List<PhaseDto> phases,
        List<NodeDto> nodes,
        List<EdgeDto> edges) {
}
