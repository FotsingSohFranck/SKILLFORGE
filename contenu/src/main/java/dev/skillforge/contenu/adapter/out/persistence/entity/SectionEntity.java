package dev.skillforge.contenu.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/** Entité de persistance R2DBC de la table {@code sections}. */
@Table("sections")
public record SectionEntity(
        @Id UUID id,
        @Column("lesson_id") UUID lessonId,
        String type,
        Integer position,
        String title,
        String body,
        @Column("code_language") String codeLanguage,
        @Column("diagram_format") String diagramFormat,
        String solution,
        @Column("quiz_id") UUID quizId) {
}
