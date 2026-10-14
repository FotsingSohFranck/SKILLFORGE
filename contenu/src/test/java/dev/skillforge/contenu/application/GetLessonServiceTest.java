package dev.skillforge.contenu.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.contenu.domain.model.Lesson;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.NodeId;
import dev.skillforge.contenu.domain.model.Section;
import dev.skillforge.contenu.domain.model.SectionId;
import dev.skillforge.contenu.domain.model.SectionType;
import dev.skillforge.contenu.domain.port.out.LessonRepository;
import dev.skillforge.contenu.domain.port.out.SectionRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

class GetLessonServiceTest {

    @Test
    void renvoie_404_si_lecon_absente() {
        LessonRepository lessons = new LessonRepository() {
            @Override
            public Flux<Lesson> findByNodeId(NodeId node) {
                return Flux.empty();
            }

            @Override
            public Mono<Lesson> findById(LessonId id) {
                return Mono.empty();
            }
        };
        SectionRepository sections = lesson -> Flux.empty();
        var service = new GetLessonService(lessons, sections);

        StepVerifier.create(service.get(new LessonId(UUID.randomUUID())))
                .expectErrorMatches(e -> e instanceof NotFoundException nf
                        && nf.code().equals("LESSON_NOT_FOUND"))
                .verify();
    }

    @Test
    void assemble_la_lecon_et_ses_sections() {
        LessonId id = new LessonId(UUID.randomUUID());
        Lesson lesson = new Lesson(id, new NodeId(UUID.randomUUID()), "intro", "Intro", 0);
        Section section = new Section(new SectionId(UUID.randomUUID()), SectionType.CONCEPT, 0,
                "Notion", "corps", null, null, null, null);
        LessonRepository lessons = new LessonRepository() {
            @Override
            public Flux<Lesson> findByNodeId(NodeId node) {
                return Flux.just(lesson);
            }

            @Override
            public Mono<Lesson> findById(LessonId lessonId) {
                return Mono.just(lesson);
            }
        };
        SectionRepository sections = lessonId -> Flux.just(section);

        StepVerifier.create(new GetLessonService(lessons, sections).get(id))
                .assertNext(detail -> {
                    org.junit.jupiter.api.Assertions.assertEquals("Intro", detail.lesson().title());
                    org.junit.jupiter.api.Assertions.assertEquals(1, detail.sections().size());
                })
                .verifyComplete();
    }
}
