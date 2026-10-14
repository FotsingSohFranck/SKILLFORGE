package dev.skillforge.iam.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Mise à jour du profil : nom affiché. */
public record UpdateProfileRequest(
        @NotBlank @Size(max = 120) String displayName) {
}
