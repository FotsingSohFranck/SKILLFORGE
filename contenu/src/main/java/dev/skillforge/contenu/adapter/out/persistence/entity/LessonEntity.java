package dev.skillforge.contenu.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/** Entité de persistance R2DBC de la table {@code lessons}. */
@Table("lessons")
public record LessonEntity(
        @Id UUID id,
        @Column("node_id") UUID nodeId,
        String slug,
        String title,
        Integer position) {
}
