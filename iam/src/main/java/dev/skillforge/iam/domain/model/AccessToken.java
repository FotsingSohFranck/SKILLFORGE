package dev.skillforge.iam.domain.model;

/** Jeton d'accès (JWT) et sa durée de validité. */
public record AccessToken(String value, long expiresInSeconds) {
}
