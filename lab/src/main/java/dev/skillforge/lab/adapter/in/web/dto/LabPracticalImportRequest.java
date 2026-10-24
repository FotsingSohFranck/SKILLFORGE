package dev.skillforge.lab.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corps d'import d'une épreuve pratique (pivot de contenu). {@code timeoutSeconds} et
 * {@code requiresPractical} sont optionnels (valeurs par défaut : 900 s, non requise).
 */
public record LabPracticalImportRequest(
        @NotBlank String title,
        @NotBlank String image,
        @NotBlank String instructions,
        String workspaceSeed,
        @NotBlank String validationRef,
        Integer timeoutSeconds,
        Boolean requiresPractical
) {
    public int timeoutOrDefault() {
        return timeoutSeconds == null || timeoutSeconds <= 0 ? 900 : timeoutSeconds;
    }

    public boolean requiresPracticalOrDefault() {
        return Boolean.TRUE.equals(requiresPractical);
    }
}
