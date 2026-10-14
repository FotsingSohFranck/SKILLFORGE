package dev.skillforge.catalogue.domain.port.in;

import dev.skillforge.catalogue.domain.model.Node;
import dev.skillforge.catalogue.domain.model.NodeId;
import reactor.core.publisher.Mono;

/** Obtenir un nœud par son identifiant. */
public interface GetNodeUseCase {
    Mono<Node> getById(NodeId id);
}
