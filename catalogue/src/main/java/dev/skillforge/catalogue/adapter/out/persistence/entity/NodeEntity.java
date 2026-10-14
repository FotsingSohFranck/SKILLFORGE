package dev.skillforge.catalogue.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/** Entité de persistance R2DBC de la table {@code nodes}. */
@Table("nodes")
public record NodeEntity(
        @Id UUID id,
        @Column("roadmap_id") UUID roadmapId,
        @Column("phase_id") UUID phaseId,
        String slug,
        String title,
        String summary,
        String importance,
        @Column("estimated_minutes") Integer estimatedMinutes,
        Integer position) {
}
