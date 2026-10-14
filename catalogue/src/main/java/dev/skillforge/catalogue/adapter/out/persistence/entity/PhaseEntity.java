package dev.skillforge.catalogue.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/** Entité de persistance R2DBC de la table {@code phases}. */
@Table("phases")
public record PhaseEntity(
        @Id UUID id,
        @Column("roadmap_id") UUID roadmapId,
        String title,
        Integer position) {
}
