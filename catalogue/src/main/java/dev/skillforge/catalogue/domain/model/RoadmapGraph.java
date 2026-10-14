package dev.skillforge.catalogue.domain.model;

import java.util.List;

/** Parcours complet avec son graphe : phases, nœuds et arêtes. */
public record RoadmapGraph(
        Roadmap roadmap,
        List<Phase> phases,
        List<Node> nodes,
        List<Edge> edges) {
}
