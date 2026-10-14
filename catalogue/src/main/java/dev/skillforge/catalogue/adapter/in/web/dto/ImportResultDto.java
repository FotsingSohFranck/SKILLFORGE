package dev.skillforge.catalogue.adapter.in.web.dto;

import java.util.UUID;

/** Résultat d'un import de roadmap. */
public record ImportResultDto(UUID roadmapId, String slug) {
}
