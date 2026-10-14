package dev.skillforge.iam.domain.port.out;

import dev.skillforge.iam.domain.model.UserId;
import reactor.core.publisher.Mono;

/**
 * Port sortant : jetons de réinitialisation de mot de passe.
 * Le jeton brut n'est jamais stocké (seule son empreinte l'est) ; usage unique et durée limitée.
 */
public interface PasswordResetTokenRepository {

    /** Émet un jeton pour l'utilisateur et renvoie sa valeur brute (à envoyer par e-mail). */
    Mono<String> issue(UserId userId);

    /**
     * Consomme un jeton brut valide (non expiré, non déjà utilisé), le marque comme utilisé,
     * et renvoie l'identifiant de l'utilisateur. Mono vide si le jeton est invalide.
     */
    Mono<UserId> consume(String rawToken);
}
