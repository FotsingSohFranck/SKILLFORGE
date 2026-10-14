package dev.skillforge.apprentissage.domain.model;

import java.util.List;

/** Vue de progression d'un parcours : l'inscription + la progression par nœud. */
public record RoadmapProgress(
        Enrollment enrollment,
        List<NodeProgress> nodes) {
}
