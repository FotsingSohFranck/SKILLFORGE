package dev.skillforge.iam.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Corps de requête de rafraîchissement / déconnexion. */
public record RefreshRequest(
        @NotBlank String refreshToken) {
}
