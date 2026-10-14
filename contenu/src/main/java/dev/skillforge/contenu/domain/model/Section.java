package dev.skillforge.contenu.domain.model;

import java.util.UUID;

/**
 * Section ordonnée et typée d'une leçon. Selon {@code type} :
 * CONCEPT → body ; DIAGRAM → body + diagramFormat ; CODE → body + codeLanguage ;
 * EXERCISE → body + solution ; QUIZ → quizId (réf souple vers evaluation).
 */
public record Section(
        SectionId id,
        SectionType type,
        int position,
        String title,
        String body,
        String codeLanguage,
        DiagramFormat diagramFormat,
        String solution,
        UUID quizId) {
}
