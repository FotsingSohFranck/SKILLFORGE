package dev.skillforge.catalogue.adapter.out.persistence;

import dev.skillforge.catalogue.adapter.out.persistence.mapper.CataloguePersistenceMapper;
import dev.skillforge.catalogue.adapter.out.persistence.repository.RoadmapR2dbcRepository;
import dev.skillforge.catalogue.domain.model.Roadmap;
import dev.skillforge.catalogue.domain.port.out.RoadmapRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Adaptateur R2DBC implémentant le port sortant {@link RoadmapRepository}. */
@Component
public class RoadmapRepositoryAdapter implements RoadmapRepository {

    private final RoadmapR2dbcRepository repository;

    public RoadmapRepositoryAdapter(RoadmapR2dbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<Roadmap> findAllOrdered() {
        return repository.findAllByOrderByPositionAscTitleAsc()
                .map(CataloguePersistenceMapper::toDomain);
    }

    @Override
    public Mono<Roadmap> findBySlug(String slug) {
        return repository.findBySlug(slug).map(CataloguePersistenceMapper::toDomain);
    }

    @Override
    public Flux<Roadmap> search(String query) {
        return repository.searchByPattern("%" + query + "%")
                .map(CataloguePersistenceMapper::toDomain);
    }
}
