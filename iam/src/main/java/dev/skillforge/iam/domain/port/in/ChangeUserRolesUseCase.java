package dev.skillforge.iam.domain.port.in;

import dev.skillforge.iam.domain.model.RoleCode;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.model.UserId;
import reactor.core.publisher.Mono;

import java.util.Set;

/** Remplacer les rôles d'un utilisateur (administration). */
public interface ChangeUserRolesUseCase {
    Mono<User> changeRoles(UserId id, Set<RoleCode> roles);
}
