package dev.skillforge.iam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Paramètres du flux "mot de passe oublié" : durée de vie du jeton, URL du front
 * (pour construire le lien) et expéditeur de l'e-mail.
 */
@ConfigurationProperties("skillforge.security.reset")
public record PasswordResetProperties(long ttlMinutes, String frontendBaseUrl, String mailFrom) {

    public PasswordResetProperties {
        if (ttlMinutes <= 0) {
            ttlMinutes = 30;
        }
        if (frontendBaseUrl == null || frontendBaseUrl.isBlank()) {
            frontendBaseUrl = "http://localhost:8081";
        }
        if (mailFrom == null || mailFrom.isBlank()) {
            mailFrom = "no-reply@skillforge.local";
        }
    }
}
