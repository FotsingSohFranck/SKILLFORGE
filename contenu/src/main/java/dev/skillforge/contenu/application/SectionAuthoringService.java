package dev.skillforge.contenu.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.contenu.domain.model.DiagramFormat;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.Section;
import dev.skillforge.contenu.domain.model.SectionId;
import dev.skillforge.contenu.domain.model.SectionType;
import dev.skillforge.contenu.domain.port.in.SectionAuthoringUseCase;
import dev.skillforge.contenu.domain.port.in.SectionContent;
import dev.skillforge.contenu.domain.port.out.SectionWriteStore;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class SectionAuthoringService implements SectionAuthoringUseCase {

    private final SectionWriteStore store;

    public SectionAuthoringService(SectionWriteStore store) {
        this.store = store;
    }

    @Override
    public Mono<Section> create(LessonId lesson, SectionContent content) {
        validate(content);
        return store.insert(lesson, content);
    }

    @Override
    public Mono<Section> update(SectionId section, SectionContent content) {
        validate(content);
        return store.update(section, content)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "SECTION_NOT_FOUND", "Section introuvable : " + section.value())));
    }

    @Override
    public Mono<Void> delete(SectionId section) {
        return store.delete(section)
                .filter(rows -> rows > 0)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "SECTION_NOT_FOUND", "Section introuvable : " + section.value())))
                .then();
    }

    @Override
    public Mono<Void> reorder(LessonId lesson, List<SectionId> orderedSections) {
        return Flux.range(0, orderedSections.size())
                .concatMap(index -> store.updatePosition(orderedSections.get(index), index))
                .then();
    }

    private static void validate(SectionContent c) {
        parseEnum(SectionType.class, c.type(), "type", "CONCEPT/DIAGRAM/CODE/EXERCISE/QUIZ");
        if (c.diagramFormat() != null) {
            parseEnum(DiagramFormat.class, c.diagramFormat(), "diagramFormat", "SVG/MERMAID");
        }
    }

    private static <E extends Enum<E>> void parseEnum(Class<E> type, String value, String field, String allowed) {
        try {
            Enum.valueOf(type, value == null ? "" : value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("INVALID_SECTION",
                    "Valeur invalide pour " + field + " : '" + value + "' (attendu : " + allowed + ").");
        }
    }
}
