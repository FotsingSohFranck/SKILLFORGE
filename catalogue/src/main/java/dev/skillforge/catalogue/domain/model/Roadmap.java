package dev.skillforge.catalogue.domain.model;

/** Parcours (résumé), sans son graphe. */
public record Roadmap(
        RoadmapId id,
        String slug,
        String title,
        String summary,
        RoadmapKind kind,
        int position) {
}
