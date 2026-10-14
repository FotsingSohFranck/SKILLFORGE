package dev.skillforge.iam.domain.port.in;

import reactor.core.publisher.Mono;

/** Cas d'usage : définir un nouveau mot de passe à partir d'un jeton de réinitialisation. */
public interface ResetPasswordUseCase {

    Mono<Void> reset(String rawToken, String newPassword);
}
