package dev.skillforge.catalogue.application;

import dev.skillforge.catalogue.domain.model.Node;
import dev.skillforge.catalogue.domain.model.NodeId;
import dev.skillforge.catalogue.domain.port.in.GetNodeUseCase;
import dev.skillforge.catalogue.domain.port.out.NodeRepository;
import dev.skillforge.common.error.NotFoundException;
import reactor.core.publisher.Mono;

public class GetNodeService implements GetNodeUseCase {

    private final NodeRepository nodes;

    public GetNodeService(NodeRepository nodes) {
        this.nodes = nodes;
    }

    @Override
    public Mono<Node> getById(NodeId id) {
        return nodes.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "NODE_NOT_FOUND", "Nœud introuvable : " + id.value())));
    }
}
