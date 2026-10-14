package dev.skillforge.iam.domain.port.out;

import dev.skillforge.iam.domain.model.Email;
import reactor.core.publisher.Mono;

/** Port sortant : envoi de l'e-mail contenant le lien de réinitialisation. */
public interface PasswordResetMailer {

    Mono<Void> sendResetLink(Email recipient, String resetUrl);
}
