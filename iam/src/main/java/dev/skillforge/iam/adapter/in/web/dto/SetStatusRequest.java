package dev.skillforge.iam.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

/** Corps de requête : statut d'activation d'un utilisateur. */
public record SetStatusRequest(
        @NotNull Boolean enabled) {
}
