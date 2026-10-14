package dev.skillforge.iam.domain.port.in;

import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.model.UserId;
import reactor.core.publisher.Mono;

/** Obtenir le profil d'un utilisateur par identifiant. Erreur 404 si absent. */
public interface GetProfileUseCase {
    Mono<User> getById(UserId id);
}
