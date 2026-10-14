package dev.skillforge.iam.adapter.in.web.dto;

/** Réponse contenant la paire de jetons. */
public record TokensResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds) {
}
