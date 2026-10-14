package dev.skillforge.catalogue.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/** Entité de persistance R2DBC de la table {@code edges}. */
@Table("edges")
public record EdgeEntity(
        @Id UUID id,
        @Column("roadmap_id") UUID roadmapId,
        @Column("source_node_id") UUID sourceNodeId,
        @Column("target_node_id") UUID targetNodeId,
        String kind) {
}
