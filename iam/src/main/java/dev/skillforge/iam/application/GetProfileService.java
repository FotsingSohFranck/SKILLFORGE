package dev.skillforge.iam.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.model.UserId;
import dev.skillforge.iam.domain.port.in.GetProfileUseCase;
import dev.skillforge.iam.domain.port.out.UserRepository;
import reactor.core.publisher.Mono;

public class GetProfileService implements GetProfileUseCase {

    private final UserRepository users;

    public GetProfileService(UserRepository users) {
        this.users = users;
    }

    @Override
    public Mono<User> getById(UserId id) {
        return users.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "USER_NOT_FOUND", "Utilisateur introuvable : " + id.value())));
    }
}
