package dev.skillforge.iam.domain.port.in;

import dev.skillforge.iam.domain.model.User;
import reactor.core.publisher.Flux;

/** Lister tous les utilisateurs (administration). */
public interface ListUsersUseCase {
    Flux<User> list();
}
