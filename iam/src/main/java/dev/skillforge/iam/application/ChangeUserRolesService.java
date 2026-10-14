package dev.skillforge.iam.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.iam.domain.model.RoleCode;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.model.UserId;
import dev.skillforge.iam.domain.port.in.ChangeUserRolesUseCase;
import dev.skillforge.iam.domain.port.out.UserRepository;
import reactor.core.publisher.Mono;

import java.util.Set;

public class ChangeUserRolesService implements ChangeUserRolesUseCase {

    private final UserRepository users;

    public ChangeUserRolesService(UserRepository users) {
        this.users = users;
    }

    @Override
    public Mono<User> changeRoles(UserId id, Set<RoleCode> roles) {
        if (roles == null || roles.isEmpty()) {
            return Mono.error(new BusinessRuleException(
                    "EMPTY_ROLES", "Un utilisateur doit avoir au moins un rôle."));
        }
        return users.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "USER_NOT_FOUND", "Utilisateur introuvable : " + id.value())))
                .flatMap(user -> users.replaceRoles(id, roles).then(users.findById(id)));
    }
}
