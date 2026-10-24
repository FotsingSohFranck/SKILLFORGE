package dev.skillforge.lab.domain.port.out;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port sortant vers le catalogue (lecture de la politique du nœud).
 *
 * <p>Couplage temporaire : l'adapter lit directement {@code nodes.completion_policy}, comme
 * {@code apprentissage} le fait pour ses compteurs. À re-router vers un service catalogue plus tard.
 */
public interface LabCatalogPort {

    /** Vrai si la politique du nœud est {@code LESSONS_AND_PRACTICAL}. */
    Mono<Boolean> requiresPractical(UUID nodeId);

    /**
     * Fixe la politique de validation du nœud : {@code LESSONS_AND_PRACTICAL} si l'épreuve
     * pratique est requise pour valider, sinon {@code LESSONS_ONLY}. Utilisé par l'import.
     */
    Mono<Void> setCompletionPolicy(UUID nodeId, boolean requiresPractical);
}
