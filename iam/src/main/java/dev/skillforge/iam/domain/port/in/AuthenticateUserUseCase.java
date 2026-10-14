package dev.skillforge.iam.domain.port.in;

import dev.skillforge.iam.domain.model.User;
import reactor.core.publisher.Mono;

/** Authentifier un utilisateur par e-mail + mot de passe. Erreur 401 si invalide. */
public interface AuthenticateUserUseCase {
    Mono<User> authenticate(String email, String rawPassword);
}
