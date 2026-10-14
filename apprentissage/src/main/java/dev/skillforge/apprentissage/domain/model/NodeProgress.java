package dev.skillforge.apprentissage.domain.model;

/** Progression d'un nœud : statut, % et dernière leçon consultée (reprise exacte). */
public record NodeProgress(
        NodeId nodeId,
        ProgressStatus status,
        double percent,
        LessonId lastLessonId) {
}
