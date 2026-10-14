package dev.skillforge.contenu.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.contenu.domain.model.LessonDetail;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.port.in.GetLessonUseCase;
import dev.skillforge.contenu.domain.port.out.LessonRepository;
import dev.skillforge.contenu.domain.port.out.SectionRepository;
import reactor.core.publisher.Mono;

public class GetLessonService implements GetLessonUseCase {

    private final LessonRepository lessons;
    private final SectionRepository sections;

    public GetLessonService(LessonRepository lessons, SectionRepository sections) {
        this.lessons = lessons;
        this.sections = sections;
    }

    @Override
    public Mono<LessonDetail> get(LessonId lesson) {
        return lessons.findById(lesson)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "LESSON_NOT_FOUND", "Leçon introuvable : " + lesson.value())))
                .flatMap(found -> sections.findByLessonId(lesson).collectList()
                        .map(list -> new LessonDetail(found, list)));
    }
}
