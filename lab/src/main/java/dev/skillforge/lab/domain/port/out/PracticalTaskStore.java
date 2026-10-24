package dev.skillforge.lab.domain.port.out;

import dev.skillforge.lab.domain.model.PracticalTask;
import dev.skillforge.lab.domain.model.PracticalTaskId;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Port sortant : lecture des définitions d'épreuves pratiques. */
public interface PracticalTaskStore {

    /** Épreuve pratique rattachée à un nœud, ou {@link Mono#empty()} si le nœud n'en a pas. */
    Mono<PracticalTask> findByNode(UUID nodeId);

    Mono<PracticalTask> findById(PracticalTaskId id);

    /**
     * Crée ou met à jour l'épreuve pratique d'un nœud (une seule par nœud : clé unique
     * {@code node_id}). Utilisé par l'import de contenu ; idempotent.
     */
    Mono<Void> upsert(PracticalTask task);
}
