package dev.skillforge.iam.domain.port.in;

import dev.skillforge.iam.domain.model.AuthTokens;
import reactor.core.publisher.Mono;

/** Connexion : authentifie puis émet une paire de jetons. */
public interface LoginUseCase {
    Mono<AuthTokens> login(String email, String rawPassword);
}
