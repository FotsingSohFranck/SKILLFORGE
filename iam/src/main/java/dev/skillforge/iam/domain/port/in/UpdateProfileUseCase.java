package dev.skillforge.iam.domain.port.in;

import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.model.UserId;
import reactor.core.publisher.Mono;

/** Cas d'usage : mettre à jour son profil (nom affiché). */
public interface UpdateProfileUseCase {

    Mono<User> updateDisplayName(UserId id, String displayName);
}
