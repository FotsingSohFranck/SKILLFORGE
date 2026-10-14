package dev.skillforge.iam.domain.port.out;

import dev.skillforge.iam.domain.model.PasswordHash;

/** Port sortant : hachage et vérification des mots de passe. */
public interface PasswordHasher {

    PasswordHash hash(String rawPassword);

    boolean matches(String rawPassword, PasswordHash hash);
}
