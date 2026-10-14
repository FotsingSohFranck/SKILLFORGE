package dev.skillforge.contenu.domain.port.in;

import dev.skillforge.contenu.domain.model.Lesson;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.NodeId;
import reactor.core.publisher.Mono;

import java.util.List;

/** Authoring des leçons (INSTRUCTOR/ADMIN) : créer, éditer, supprimer, réordonner. */
public interface LessonAuthoringUseCase {

    Mono<Lesson> create(NodeId node, String slug, String title, Integer position);

    Mono<Lesson> update(LessonId lesson, String slug, String title, Integer position);

    Mono<Void> delete(LessonId lesson);

    /** Réordonne les leçons d'un nœud selon l'ordre fourni (position = index). */
    Mono<Void> reorder(NodeId node, List<LessonId> orderedLessons);
}
