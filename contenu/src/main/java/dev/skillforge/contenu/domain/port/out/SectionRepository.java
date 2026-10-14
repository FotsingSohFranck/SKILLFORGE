package dev.skillforge.contenu.domain.port.out;

import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.Section;
import reactor.core.publisher.Flux;

/** Port sortant : accès aux sections. */
public interface SectionRepository {

    Flux<Section> findByLessonId(LessonId lesson);
}
