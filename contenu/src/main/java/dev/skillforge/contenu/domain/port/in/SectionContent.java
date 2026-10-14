package dev.skillforge.contenu.domain.port.in;

import java.util.UUID;

/** Données d'entrée d'une section (création/édition). Le {@code type} est validé par le service. */
public record SectionContent(
        String type,
        Integer position,
        String title,
        String body,
        String codeLanguage,
        String diagramFormat,
        String solution,
        UUID quizId) {
}
