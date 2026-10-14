package dev.skillforge.catalogue.domain.port.out;

import dev.skillforge.catalogue.domain.model.Node;
import dev.skillforge.catalogue.domain.model.NodeId;
import dev.skillforge.catalogue.domain.model.RoadmapId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Port sortant : accès aux nœuds. */
public interface NodeRepository {

    Flux<Node> findByRoadmapId(RoadmapId roadmapId);

    Mono<Node> findById(NodeId id);
}
