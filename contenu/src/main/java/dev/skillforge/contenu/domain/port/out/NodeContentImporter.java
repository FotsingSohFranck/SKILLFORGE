package dev.skillforge.contenu.domain.port.out;

import dev.skillforge.contenu.domain.port.in.NodeContentImport;
import reactor.core.publisher.Mono;

/**
 * Port sortant : persistance idempotente du contenu d'un nœud (upsert leçons par
 * {@code (node_id, slug)} avec id préservé ; sections remplacées).
 */
public interface NodeContentImporter {
    Mono<Integer> importContent(NodeContentImport command);
}
