package dev.skillforge.catalogue.adapter.out.persistence;

import dev.skillforge.catalogue.adapter.out.persistence.mapper.CataloguePersistenceMapper;
import dev.skillforge.catalogue.adapter.out.persistence.repository.PhaseR2dbcRepository;
import dev.skillforge.catalogue.domain.model.Phase;
import dev.skillforge.catalogue.domain.model.RoadmapId;
import dev.skillforge.catalogue.domain.port.out.PhaseRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/** Adaptateur R2DBC implémentant le port sortant {@link PhaseRepository}. */
@Component
public class PhaseRepositoryAdapter implements PhaseRepository {

    private final PhaseR2dbcRepository repository;

    public PhaseRepositoryAdapter(PhaseR2dbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<Phase> findByRoadmapId(RoadmapId roadmapId) {
        return repository.findByRoadmapIdOrderByPositionAsc(roadmapId.value())
                .map(CataloguePersistenceMapper::toDomain);
    }
}
