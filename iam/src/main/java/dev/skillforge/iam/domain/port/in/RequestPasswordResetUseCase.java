package dev.skillforge.iam.domain.port.in;

import reactor.core.publisher.Mono;

/**
 * Cas d'usage : demander une réinitialisation (flux "mot de passe oublié").
 * Réponse neutre : ne révèle jamais si l'adresse correspond à un compte.
 */
public interface RequestPasswordResetUseCase {

    Mono<Void> request(String email);
}
