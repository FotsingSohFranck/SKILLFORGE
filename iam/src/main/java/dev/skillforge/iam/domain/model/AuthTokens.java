package dev.skillforge.iam.domain.model;

/** Paire de jetons émise à la connexion / au rafraîchissement. */
public record AuthTokens(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds) {

    public static AuthTokens bearer(String accessToken, String refreshToken, long expiresInSeconds) {
        return new AuthTokens(accessToken, refreshToken, "Bearer", expiresInSeconds);
    }
}
