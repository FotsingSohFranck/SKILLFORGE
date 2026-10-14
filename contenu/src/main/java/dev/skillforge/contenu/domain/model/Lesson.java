package dev.skillforge.contenu.domain.model;

/** Leçon appartenant à un nœud. */
public record Lesson(
        LessonId id,
        NodeId nodeId,
        String slug,
        String title,
        int position) {
}
