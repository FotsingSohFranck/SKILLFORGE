package dev.skillforge.contenu.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.contenu.domain.model.Lesson;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.NodeId;
import dev.skillforge.contenu.domain.model.Section;
import dev.skillforge.contenu.domain.model.SectionId;
import dev.skillforge.contenu.domain.port.in.SectionContent;
import dev.skillforge.contenu.domain.port.out.LessonWriteStore;
import dev.skillforge.contenu.domain.port.out.SectionWriteStore;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthoringServicesTest {

    @Test
    void create_lesson_rejette_slug_vide() {
        var service = new LessonAuthoringService(new NoopLessonStore());
        assertThrows(BusinessRuleException.class,
                () -> service.create(new NodeId(UUID.randomUUID()), " ", "Titre", 0));
    }

    @Test
    void delete_lesson_absente_renvoie_404() {
        var service = new LessonAuthoringService(new NoopLessonStore()); // delete -> 0 rows
        StepVerifier.create(service.delete(new LessonId(UUID.randomUUID())))
                .expectErrorMatches(e -> e instanceof NotFoundException nf
                        && nf.code().equals("LESSON_NOT_FOUND"))
                .verify();
    }

    @Test
    void create_section_rejette_type_invalide() {
        var service = new SectionAuthoringService(new NoopSectionStore());
        assertThrows(BusinessRuleException.class,
                () -> service.create(new LessonId(UUID.randomUUID()),
                        new SectionContent("SUPER", 0, null, null, null, null, null, null)));
    }

    static final class NoopLessonStore implements LessonWriteStore {
        @Override
        public Mono<Lesson> insert(NodeId node, String slug, String title, int position) {
            return Mono.just(new Lesson(new LessonId(UUID.randomUUID()), node, slug, title, position));
        }

        @Override
        public Mono<Lesson> update(LessonId lesson, String slug, String title, int position) {
            return Mono.empty();
        }

        @Override
        public Mono<Long> delete(LessonId lesson) {
            return Mono.just(0L);
        }

        @Override
        public Mono<Void> updatePosition(LessonId lesson, int position) {
            return Mono.empty();
        }
    }

    static final class NoopSectionStore implements SectionWriteStore {
        @Override
        public Mono<Section> insert(LessonId lesson, SectionContent content) {
            return Mono.empty();
        }

        @Override
        public Mono<Section> update(SectionId section, SectionContent content) {
            return Mono.empty();
        }

        @Override
        public Mono<Long> delete(SectionId section) {
            return Mono.just(0L);
        }

        @Override
        public Mono<Void> updatePosition(SectionId section, int position) {
            return Mono.empty();
        }
    }
}
