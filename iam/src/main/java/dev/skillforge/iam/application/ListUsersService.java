package dev.skillforge.iam.application;

import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.port.in.ListUsersUseCase;
import dev.skillforge.iam.domain.port.out.UserRepository;
import reactor.core.publisher.Flux;

public class ListUsersService implements ListUsersUseCase {

    private final UserRepository users;

    public ListUsersService(UserRepository users) {
        this.users = users;
    }

    @Override
    public Flux<User> list() {
        return users.findAll();
    }
}
