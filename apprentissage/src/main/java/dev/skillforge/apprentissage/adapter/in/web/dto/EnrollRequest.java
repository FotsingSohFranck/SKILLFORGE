package dev.skillforge.apprentissage.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Corps de requête d'inscription. */
public record EnrollRequest(@NotNull UUID roadmapId) {
}
