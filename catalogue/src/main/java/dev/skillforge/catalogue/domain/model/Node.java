package dev.skillforge.catalogue.domain.model;

/** Nœud d'un parcours (unité d'apprentissage). {@code phaseId} peut être nul. */
public record Node(
        NodeId id,
        RoadmapId roadmapId,
        PhaseId phaseId,
        String slug,
        String title,
        String summary,
        NodeImportance importance,
        Integer estimatedMinutes,
        int position) {
}
