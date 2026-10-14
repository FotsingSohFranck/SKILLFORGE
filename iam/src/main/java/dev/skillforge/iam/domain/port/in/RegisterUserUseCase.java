package dev.skillforge.iam.domain.port.in;

import dev.skillforge.iam.domain.model.User;
import reactor.core.publisher.Mono;

/** Inscrire un nouvel utilisateur (rôle LEARNER). */
public interface RegisterUserUseCase {
    Mono<User> register(RegisterUserCommand command);
}
