package dev.skillforge.catalogue.adapter.out.persistence;

import dev.skillforge.catalogue.adapter.out.persistence.mapper.CataloguePersistenceMapper;
import dev.skillforge.catalogue.adapter.out.persistence.repository.EdgeR2dbcRepository;
import dev.skillforge.catalogue.domain.model.Edge;
import dev.skillforge.catalogue.domain.model.RoadmapId;
import dev.skillforge.catalogue.domain.port.out.EdgeRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/** Adaptateur R2DBC implémentant le port sortant {@link EdgeRepository}. */
@Component
public class EdgeRepositoryAdapter implements EdgeRepository {

    private final EdgeR2dbcRepository repository;

    public EdgeRepositoryAdapter(EdgeR2dbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<Edge> findByRoadmapId(RoadmapId roadmapId) {
        return repository.findByRoadmapId(roadmapId.value())
                .map(CataloguePersistenceMapper::toDomain);
    }
}
