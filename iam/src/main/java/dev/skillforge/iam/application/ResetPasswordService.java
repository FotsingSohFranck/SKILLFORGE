package dev.skillforge.iam.application;

import dev.skillforge.common.error.UnauthorizedException;
import dev.skillforge.iam.domain.port.in.ResetPasswordUseCase;
import dev.skillforge.iam.domain.port.out.PasswordHasher;
import dev.skillforge.iam.domain.port.out.PasswordResetTokenRepository;
import dev.skillforge.iam.domain.port.out.UserRepository;
import reactor.core.publisher.Mono;

/** Définition d'un nouveau mot de passe via un jeton de réinitialisation (à usage unique). */
public class ResetPasswordService implements ResetPasswordUseCase {

    private static final UnauthorizedException INVALID_TOKEN =
            new UnauthorizedException("INVALID_RESET_TOKEN", "Lien de réinitialisation invalide ou expiré.");

    private final PasswordResetTokenRepository tokens;
    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public ResetPasswordService(PasswordResetTokenRepository tokens,
                                UserRepository users,
                                PasswordHasher passwordHasher) {
        this.tokens = tokens;
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Mono<Void> reset(String rawToken, String newPassword) {
        return tokens.consume(rawToken)
                .switchIfEmpty(Mono.error(INVALID_TOKEN))
                .flatMap(userId -> users.updatePassword(userId, passwordHasher.hash(newPassword)));
    }
}
