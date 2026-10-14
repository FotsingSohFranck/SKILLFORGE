package dev.skillforge.iam.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Réinitialisation via jeton : jeton reçu par e-mail + nouveau mot de passe. */
public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 128) String newPassword) {
}
