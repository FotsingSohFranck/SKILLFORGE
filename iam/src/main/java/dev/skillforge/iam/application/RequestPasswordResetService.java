package dev.skillforge.iam.application;

import dev.skillforge.iam.domain.model.Email;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.port.in.RequestPasswordResetUseCase;
import dev.skillforge.iam.domain.port.out.PasswordResetMailer;
import dev.skillforge.iam.domain.port.out.PasswordResetTokenRepository;
import dev.skillforge.iam.domain.port.out.UserRepository;
import reactor.core.publisher.Mono;

/**
 * Demande de réinitialisation ("mot de passe oublié"). Réponse TOUJOURS neutre : si l'adresse
 * est invalide ou inconnue, on complète sans rien faire (pas de fuite d'existence de compte).
 * L'envoi de l'e-mail est best-effort : une panne de messagerie n'échoue pas la requête.
 */
public class RequestPasswordResetService implements RequestPasswordResetUseCase {

    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordResetMailer mailer;
    private final String frontendBaseUrl;

    public RequestPasswordResetService(UserRepository users,
                                       PasswordResetTokenRepository tokens,
                                       PasswordResetMailer mailer,
                                       String frontendBaseUrl) {
        this.users = users;
        this.tokens = tokens;
        this.mailer = mailer;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Override
    public Mono<Void> request(String email) {
        Email normalized;
        try {
            normalized = new Email(email);
        } catch (RuntimeException ex) {
            return Mono.empty(); // adresse invalide : réponse neutre
        }
        return users.findByEmail(normalized)
                .filter(User::enabled)
                .flatMap(user -> tokens.issue(user.id())
                        .flatMap(raw -> mailer.sendResetLink(user.email(), resetUrl(raw)))
                        .onErrorResume(ex -> Mono.empty())) // envoi best-effort
                .then();
    }

    private String resetUrl(String rawToken) {
        String base = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        return base + "/reset-password?token=" + rawToken;
    }
}
