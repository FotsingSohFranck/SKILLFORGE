package dev.skillforge.contenu.domain.port.out;

import dev.skillforge.contenu.domain.model.Lesson;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.NodeId;
import reactor.core.publisher.Mono;

/** Port sortant : écriture des leçons. */
public interface LessonWriteStore {

    Mono<Lesson> insert(NodeId node, String slug, String title, int position);

    /** Met à jour une leçon ; vide si l'id n'existe pas. */
    Mono<Lesson> update(LessonId lesson, String slug, String title, int position);

    Mono<Long> delete(LessonId lesson);

    Mono<Void> updatePosition(LessonId lesson, int position);
}
