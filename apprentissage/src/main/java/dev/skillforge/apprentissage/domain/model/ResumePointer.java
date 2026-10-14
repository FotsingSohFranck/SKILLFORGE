package dev.skillforge.apprentissage.domain.model;

/**
 * Point de reprise globale : dernier parcours actif de l'apprenant et sa position
 * (nœud + leçon). Les champs peuvent être nuls si aucune activité.
 */
public record ResumePointer(
        RoadmapId roadmapId,
        NodeId nodeId,
        LessonId lessonId) {
}
