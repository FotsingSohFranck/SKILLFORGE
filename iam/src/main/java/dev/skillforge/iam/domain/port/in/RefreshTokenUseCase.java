package dev.skillforge.iam.domain.port.in;

import dev.skillforge.iam.domain.model.AuthTokens;
import reactor.core.publisher.Mono;

/** Rafraîchissement : effectue la rotation du refresh token et émet une nouvelle paire. */
public interface RefreshTokenUseCase {
    Mono<AuthTokens> refresh(String refreshToken);
}
