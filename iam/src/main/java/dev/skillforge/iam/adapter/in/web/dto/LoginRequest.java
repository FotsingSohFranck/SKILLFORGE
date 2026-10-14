package dev.skillforge.iam.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Corps de requête de connexion. */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password) {
}
