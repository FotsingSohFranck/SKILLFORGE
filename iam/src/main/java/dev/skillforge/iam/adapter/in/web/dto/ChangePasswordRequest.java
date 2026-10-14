package dev.skillforge.iam.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Changement de mot de passe (utilisateur connecté) : mot de passe actuel + nouveau. */
public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8, max = 128) String newPassword) {
}
