package dev.skillforge.contenu.domain.port.in;

import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.Section;
import dev.skillforge.contenu.domain.model.SectionId;
import reactor.core.publisher.Mono;

import java.util.List;

/** Authoring des sections (INSTRUCTOR/ADMIN) : créer, éditer, supprimer, réordonner. */
public interface SectionAuthoringUseCase {

    Mono<Section> create(LessonId lesson, SectionContent content);

    Mono<Section> update(SectionId section, SectionContent content);

    Mono<Void> delete(SectionId section);

    /** Réordonne les sections d'une leçon selon l'ordre fourni (position = index). */
    Mono<Void> reorder(LessonId lesson, List<SectionId> orderedSections);
}
