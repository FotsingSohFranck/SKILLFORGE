package dev.skillforge.contenu.adapter.in.web.dto;

import java.util.UUID;

/** Résultat d'un import de contenu. */
public record ContentImportResultDto(UUID nodeId, int lessonCount) {
}
