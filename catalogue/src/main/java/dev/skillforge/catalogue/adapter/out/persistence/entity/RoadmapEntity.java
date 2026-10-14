package dev.skillforge.catalogue.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/** Entité de persistance R2DBC de la table {@code roadmaps} (colonnes utiles à la lecture). */
@Table("roadmaps")
public record RoadmapEntity(
        @Id UUID id,
        String slug,
        String title,
        String summary,
        String kind,
        Integer position) {
}
