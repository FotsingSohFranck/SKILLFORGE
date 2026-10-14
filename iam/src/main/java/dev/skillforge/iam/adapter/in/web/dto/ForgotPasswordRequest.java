package dev.skillforge.iam.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Demande de réinitialisation : adresse e-mail du compte. */
public record ForgotPasswordRequest(
        @NotBlank @Email String email) {
}
