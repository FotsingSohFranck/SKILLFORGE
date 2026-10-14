package dev.skillforge.iam.application;

import dev.skillforge.common.error.UnauthorizedException;
import dev.skillforge.iam.domain.model.UserId;
import dev.skillforge.iam.domain.port.in.ChangePasswordUseCase;
import dev.skillforge.iam.domain.port.out.PasswordHasher;
import dev.skillforge.iam.domain.port.out.UserRepository;
import reactor.core.publisher.Mono;

/** Changement de mot de passe d'un utilisateur connecté : vérifie l'actuel avant de remplacer. */
public class ChangePasswordService implements ChangePasswordUseCase {

    private static final UnauthorizedException INVALID_CURRENT =
            new UnauthorizedException("INVALID_CURRENT_PASSWORD", "Mot de passe actuel incorrect.");

    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public ChangePasswordService(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Mono<Void> change(UserId userId, String currentPassword, String newPassword) {
        return users.findById(userId)
                .switchIfEmpty(Mono.error(INVALID_CURRENT))
                .flatMap(user -> {
                    if (!passwordHasher.matches(currentPassword, user.passwordHash())) {
                        return Mono.error(INVALID_CURRENT);
                    }
                    return users.updatePassword(userId, passwordHasher.hash(newPassword));
                });
    }
}
