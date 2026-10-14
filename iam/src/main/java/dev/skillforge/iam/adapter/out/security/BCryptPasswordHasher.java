package dev.skillforge.iam.adapter.out.security;

import dev.skillforge.iam.domain.model.PasswordHash;
import dev.skillforge.iam.domain.port.out.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptateur de hachage BCrypt (implémente {@link PasswordHasher}).
 * Compatible avec les hash {@code $2y$} du seed (SF-10c). Le hachage BCrypt est purement
 * CPU (pas d'I/O) : son appel synchrone est acceptable dans un pipeline réactif.
 */
@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public PasswordHash hash(String rawPassword) {
        return new PasswordHash(encoder.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash hash) {
        return encoder.matches(rawPassword, hash.value());
    }
}
