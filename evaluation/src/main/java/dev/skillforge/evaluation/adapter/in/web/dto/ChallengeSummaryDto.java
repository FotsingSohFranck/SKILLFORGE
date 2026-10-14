package dev.skillforge.evaluation.adapter.in.web.dto;

import java.util.UUID;

/** Résumé d'un défi de code exposé dans la liste d'un nœud. */
public record ChallengeSummaryDto(UUID id, String title, String language) {
}
