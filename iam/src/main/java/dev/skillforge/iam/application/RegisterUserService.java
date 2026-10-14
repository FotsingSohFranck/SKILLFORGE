package dev.skillforge.iam.application;

import dev.skillforge.common.error.ConflictException;
import dev.skillforge.iam.domain.model.Email;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.port.in.RegisterUserCommand;
import dev.skillforge.iam.domain.port.in.RegisterUserUseCase;
import dev.skillforge.iam.domain.port.out.PasswordHasher;
import dev.skillforge.iam.domain.port.out.UserRepository;
import reactor.core.publisher.Mono;

public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public RegisterUserService(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Mono<User> register(RegisterUserCommand command) {
        Email email = new Email(command.email()); // valide + normalise
        String displayName = command.displayName() == null || command.displayName().isBlank()
                ? email.value()
                : command.displayName().trim();
        return users.existsByEmail(email)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new ConflictException(
                                "EMAIL_ALREADY_USED", "Un compte existe déjà avec cet e-mail."));
                    }
                    User user = User.register(email, passwordHasher.hash(command.rawPassword()), displayName);
                    return users.save(user);
                });
    }
}
