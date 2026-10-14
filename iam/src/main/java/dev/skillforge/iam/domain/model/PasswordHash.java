package dev.skillforge.iam.domain.model;

/** Empreinte (hash) d'un mot de passe. Le mot de passe en clair ne transite jamais ici. */
public record PasswordHash(String value) {

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("password hash must not be blank");
        }
    }
}
