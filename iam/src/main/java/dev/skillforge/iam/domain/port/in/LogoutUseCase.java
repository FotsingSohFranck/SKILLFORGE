package dev.skillforge.iam.domain.port.in;

import reactor.core.publisher.Mono;

/** Déconnexion : révoque le refresh token fourni. */
public interface LogoutUseCase {
    Mono<Void> logout(String refreshToken);
}
