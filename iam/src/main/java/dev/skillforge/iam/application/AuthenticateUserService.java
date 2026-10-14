package dev.skillforge.iam.application;

import dev.skillforge.common.error.UnauthorizedException;
import dev.skillforge.iam.domain.model.Email;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.port.in.AuthenticateUserUseCase;
import dev.skillforge.iam.domain.port.out.PasswordHasher;
import dev.skillforge.iam.domain.port.out.UserRepository;
import reactor.core.publisher.Mono;

public class AuthenticateUserService implements AuthenticateUserUseCase {

    private static final UnauthorizedException INVALID =
            new UnauthorizedException("INVALID_CREDENTIALS", "Identifiants invalides.");

    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public AuthenticateUserService(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Mono<User> authenticate(String email, String rawPassword) {
        Email normalized;
        try {
            normalized = new Email(email);
        } catch (RuntimeException ex) {
            return Mono.error(INVALID); // ne pas divulguer la raison précise
        }
        return users.findByEmail(normalized)
                .filter(User::enabled)
                .filter(user -> passwordHasher.matches(rawPassword, user.passwordHash()))
                .switchIfEmpty(Mono.error(INVALID));
    }
}
