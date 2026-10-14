package dev.skillforge.apprentissage.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Corps de requête : bascule d'un favori (targetType = NODE | LESSON). */
public record ToggleBookmarkRequest(
        @NotBlank String targetType,
        @NotNull UUID targetId) {
}
