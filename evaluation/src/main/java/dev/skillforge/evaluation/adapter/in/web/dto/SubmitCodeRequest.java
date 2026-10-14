package dev.skillforge.evaluation.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

/** Corps de requête : code soumis pour un défi. */
public record SubmitCodeRequest(@NotNull String code) {
}
