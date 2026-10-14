package dev.skillforge.contenu.domain.port.in;

import dev.skillforge.contenu.domain.model.NodeId;

import java.util.List;
import java.util.UUID;

/**
 * Commande d'import du contenu d'un nœud (image du JSON pivot, cf. docs/PIVOT-CONTENU.md).
 * Le {@code nodeId} provient de l'URL ; le corps ne fournit que {@code lessons}.
 */
public record NodeContentImport(
        NodeId nodeId,
        List<LessonImport> lessons) {

    public record LessonImport(
            String slug,
            String title,
            Integer position,
            List<SectionImport> sections) {
    }

    public record SectionImport(
            String type,
            Integer position,
            String title,
            String body,
            String codeLanguage,
            String diagramFormat,
            String solution,
            UUID quizId) {
    }
}
