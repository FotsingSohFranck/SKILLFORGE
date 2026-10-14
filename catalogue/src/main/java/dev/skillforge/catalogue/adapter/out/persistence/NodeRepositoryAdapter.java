package dev.skillforge.catalogue.adapter.out.persistence;

import dev.skillforge.catalogue.adapter.out.persistence.mapper.CataloguePersistenceMapper;
import dev.skillforge.catalogue.adapter.out.persistence.repository.NodeR2dbcRepository;
import dev.skillforge.catalogue.domain.model.Node;
import dev.skillforge.catalogue.domain.model.NodeId;
import dev.skillforge.catalogue.domain.model.RoadmapId;
import dev.skillforge.catalogue.domain.port.out.NodeRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Adaptateur R2DBC implémentant le port sortant {@link NodeRepository}. */
@Component
public class NodeRepositoryAdapter implements NodeRepository {

    private final NodeR2dbcRepository repository;

    public NodeRepositoryAdapter(NodeR2dbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<Node> findByRoadmapId(RoadmapId roadmapId) {
        return repository.findByRoadmapIdOrderByPositionAsc(roadmapId.value())
                .map(CataloguePersistenceMapper::toDomain);
    }

    @Override
    public Mono<Node> findById(NodeId id) {
        return repository.findById(id.value()).map(CataloguePersistenceMapper::toDomain);
    }
}
