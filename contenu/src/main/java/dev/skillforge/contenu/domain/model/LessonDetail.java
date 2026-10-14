package dev.skillforge.contenu.domain.model;

import java.util.List;

/** Leçon complète avec ses sections ordonnées (rendu pour le front). */
public record LessonDetail(
        Lesson lesson,
        List<Section> sections) {
}
