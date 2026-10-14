package dev.skillforge.apprentissage.domain.model;

import java.util.UUID;

/** Identifiant typé d'une leçon (réf souple vers contenu.lessons). */
public record LessonId(UUID value) {
}
