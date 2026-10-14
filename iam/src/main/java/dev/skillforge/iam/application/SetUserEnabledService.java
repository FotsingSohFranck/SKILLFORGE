package dev.skillforge.iam.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.model.UserId;
import dev.skillforge.iam.domain.port.in.SetUserEnabledUseCase;
import dev.skillforge.iam.domain.port.out.UserRepository;
import reactor.core.publisher.Mono;

public class SetUserEnabledService implements SetUserEnabledUseCase {

    private final UserRepository users;

    public SetUserEnabledService(UserRepository users) {
        this.users = users;
    }

    @Override
    public Mono<User> setEnabled(UserId id, boolean enabled) {
        return users.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "USER_NOT_FOUND", "Utilisateur introuvable : " + id.value())))
                .flatMap(user -> users.setEnabled(id, enabled).then(users.findById(id)));
    }
}
