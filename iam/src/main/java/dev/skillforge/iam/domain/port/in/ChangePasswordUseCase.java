package dev.skillforge.iam.domain.port.in;

import dev.skillforge.iam.domain.model.UserId;
import reactor.core.publisher.Mono;

/** Cas d'usage : changer son mot de passe en étant connecté (vérifie le mot de passe actuel). */
public interface ChangePasswordUseCase {

    Mono<Void> change(UserId userId, String currentPassword, String newPassword);
}
