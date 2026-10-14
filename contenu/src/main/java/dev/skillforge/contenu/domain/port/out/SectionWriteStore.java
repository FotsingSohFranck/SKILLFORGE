package dev.skillforge.contenu.domain.port.out;

import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.Section;
import dev.skillforge.contenu.domain.model.SectionId;
import dev.skillforge.contenu.domain.port.in.SectionContent;
import reactor.core.publisher.Mono;

/** Port sortant : écriture des sections. */
public interface SectionWriteStore {

    Mono<Section> insert(LessonId lesson, SectionContent content);

    /** Met à jour une section ; vide si l'id n'existe pas. */
    Mono<Section> update(SectionId section, SectionContent content);

    Mono<Long> delete(SectionId section);

    Mono<Void> updatePosition(SectionId section, int position);
}
