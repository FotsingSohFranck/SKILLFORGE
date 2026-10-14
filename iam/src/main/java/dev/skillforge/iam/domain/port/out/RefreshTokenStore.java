package dev.skillforge.iam.domain.port.out;

import dev.skillforge.iam.domain.model.UserId;
import reactor.core.publisher.Mono;

/**
 * Port sortant : cycle de vie des refresh tokens (opaques, stockés hachés).
 */
public interface RefreshTokenStore {

    /** Émet un nouveau refresh token pour l'utilisateur et le persiste (haché). Renvoie le token brut. */
    Mono<String> issue(UserId userId);

    /** Valide un refresh token, le révoque (rotation) et renvoie l'utilisateur. Erreur 401 si invalide. */
    Mono<UserId> rotate(String rawToken);

    /** Révoque un refresh token (idempotent). */
    Mono<Void> revoke(String rawToken);
}
