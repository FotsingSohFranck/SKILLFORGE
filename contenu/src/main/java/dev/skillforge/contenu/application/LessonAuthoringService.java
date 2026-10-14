package dev.skillforge.contenu.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.contenu.domain.model.Lesson;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.NodeId;
import dev.skillforge.contenu.domain.port.in.LessonAuthoringUseCase;
import dev.skillforge.contenu.domain.port.out.LessonWriteStore;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class LessonAuthoringService implements LessonAuthoringUseCase {

    private final LessonWriteStore store;

    public LessonAuthoringService(LessonWriteStore store) {
        this.store = store;
    }

    @Override
    public Mono<Lesson> create(NodeId node, String slug, String title, Integer position) {
        validate(slug, title);
        return store.insert(node, slug.trim(), title.trim(), position == null ? 0 : position);
    }

    @Override
    public Mono<Lesson> update(LessonId lesson, String slug, String title, Integer position) {
        validate(slug, title);
        return store.update(lesson, slug.trim(), title.trim(), position == null ? 0 : position)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "LESSON_NOT_FOUND", "Leçon introuvable : " + lesson.value())));
    }

    @Override
    public Mono<Void> delete(LessonId lesson) {
        return store.delete(lesson)
                .filter(rows -> rows > 0)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "LESSON_NOT_FOUND", "Leçon introuvable : " + lesson.value())))
                .then();
    }

    @Override
    public Mono<Void> reorder(NodeId node, List<LessonId> orderedLessons) {
        return Flux.range(0, orderedLessons.size())
                .concatMap(index -> store.updatePosition(orderedLessons.get(index), index))
                .then();
    }

    private static void validate(String slug, String title) {
        if (slug == null || slug.isBlank() || title == null || title.isBlank()) {
            throw new BusinessRuleException("INVALID_LESSON", "slug et title sont requis.");
        }
    }
}
