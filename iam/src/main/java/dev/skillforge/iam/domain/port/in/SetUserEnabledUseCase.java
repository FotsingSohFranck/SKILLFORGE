package dev.skillforge.iam.domain.port.in;

import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.model.UserId;
import reactor.core.publisher.Mono;

/** Activer / désactiver un utilisateur (administration). */
public interface SetUserEnabledUseCase {
    Mono<User> setEnabled(UserId id, boolean enabled);
}
