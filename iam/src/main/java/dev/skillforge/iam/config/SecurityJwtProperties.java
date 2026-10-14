package dev.skillforge.iam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Paramètres JWT (secret HS256 + durées de vie). Fournis via {@code skillforge.security.jwt.*}.
 * Le secret DOIT faire au moins 32 octets (256 bits) pour HS256.
 */
@ConfigurationProperties(prefix = "skillforge.security.jwt")
public record SecurityJwtProperties(String secret, long accessTtlMinutes, long refreshTtlDays) {

    public SecurityJwtProperties {
        if (accessTtlMinutes <= 0) {
            accessTtlMinutes = 60;
        }
        if (refreshTtlDays <= 0) {
            refreshTtlDays = 30;
        }
    }
}
