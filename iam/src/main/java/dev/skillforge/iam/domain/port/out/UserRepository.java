package dev.skillforge.iam.domain.port.out;

import dev.skillforge.iam.domain.model.Email;
import dev.skillforge.iam.domain.model.PasswordHash;
import dev.skillforge.iam.domain.model.RoleCode;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.model.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

/** Port sortant : persistance des utilisateurs et de leurs rôles. */
public interface UserRepository {

    Mono<Boolean> existsByEmail(Email email);

    Mono<User> findByEmail(Email email);

    Mono<User> findById(UserId id);

    /** Persiste un nouvel utilisateur (et ses rôles). Renvoie l'utilisateur enregistré. */
    Mono<User> save(User user);

    /** Liste tous les utilisateurs (administration). */
    Flux<User> findAll();

    /** Remplace l'ensemble des rôles d'un utilisateur. */
    Mono<Void> replaceRoles(UserId id, Set<RoleCode> roles);

    /** Active ou désactive un utilisateur. */
    Mono<Void> setEnabled(UserId id, boolean enabled);

    /** Remplace l'empreinte du mot de passe d'un utilisateur. */
    Mono<Void> updatePassword(UserId id, PasswordHash passwordHash);

    /** Met à jour le nom affiché d'un utilisateur. */
    Mono<Void> updateDisplayName(UserId id, String displayName);
}
