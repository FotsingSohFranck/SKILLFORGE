package dev.skillforge.contenu.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.contenu.domain.model.DiagramFormat;
import dev.skillforge.contenu.domain.model.SectionType;
import dev.skillforge.contenu.domain.port.in.IngestNodeContentUseCase;
import dev.skillforge.contenu.domain.port.in.NodeContentImport;
import dev.skillforge.contenu.domain.port.out.NodeContentImporter;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Valide le pivot de contenu puis délègue la persistance idempotente à {@link NodeContentImporter}.
 * Échec de validation → {@link BusinessRuleException} (code {@code INVALID_CONTENT}) → HTTP 422.
 */
public class IngestNodeContentService implements IngestNodeContentUseCase {

    private final NodeContentImporter importer;

    public IngestNodeContentService(NodeContentImporter importer) {
        this.importer = importer;
    }

    @Override
    public Mono<Integer> ingest(NodeContentImport command) {
        try {
            validate(command);
        } catch (BusinessRuleException ex) {
            return Mono.error(ex);
        }
        return importer.importContent(command);
    }

    private void validate(NodeContentImport c) {
        require(c != null && c.nodeId() != null, "Le nœud cible est requis.");
        Set<String> slugs = new HashSet<>();
        for (NodeContentImport.LessonImport lesson : safe(c.lessons())) {
            require(notBlank(lesson.slug()), "Chaque leçon doit avoir un slug.");
            require(notBlank(lesson.title()), "Chaque leçon doit avoir un titre.");
            require(slugs.add(lesson.slug()), "Slug de leçon en double : " + lesson.slug());
            validateSections(lesson);
        }
    }

    private void validateSections(NodeContentImport.LessonImport lesson) {
        Set<Integer> positions = new HashSet<>();
        for (NodeContentImport.SectionImport section : safe(lesson.sections())) {
            parseEnum(SectionType.class, section.type(), "type",
                    "CONCEPT/DIAGRAM/CODE/EXERCISE/QUIZ/IMAGE");
            int position = section.position() == null ? 0 : section.position();
            require(positions.add(position),
                    "Position de section en double (" + position + ") dans la leçon " + lesson.slug());
            if (section.diagramFormat() != null) {
                parseEnum(DiagramFormat.class, section.diagramFormat(), "diagramFormat", "SVG/MERMAID");
            }
        }
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new BusinessRuleException("INVALID_CONTENT", message);
        }
    }

    private static <E extends Enum<E>> void parseEnum(Class<E> type, String value, String field, String allowed) {
        try {
            Enum.valueOf(type, value == null ? "" : value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("INVALID_CONTENT",
                    "Valeur invalide pour " + field + " : '" + value + "' (attendu : " + allowed + ").");
        }
    }
}
