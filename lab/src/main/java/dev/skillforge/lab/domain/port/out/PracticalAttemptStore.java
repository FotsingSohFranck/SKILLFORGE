package dev.skillforge.lab.domain.port.out;

import dev.skillforge.lab.domain.model.LearnerId;
import dev.skillforge.lab.domain.model.PracticalAttempt;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Port sortant : persistance des tentatives d'épreuve pratique. */
public interface PracticalAttemptStore {

    /** Crée ou met à jour une tentative (upsert sur l'identifiant). */
    Mono<PracticalAttempt> save(PracticalAttempt attempt);

    /** Dernière tentative d'un apprenant sur un nœud, ou {@link Mono#empty()}. */
    Mono<PracticalAttempt> findLatest(LearnerId learner, UUID nodeId);

    /** Vrai si l'apprenant a au moins une tentative réussie sur ce nœud. */
    Mono<Boolean> hasPassed(LearnerId learner, UUID nodeId);
}
