package dev.skillforge.catalogue.adapter.out.persistence.repository;

import dev.skillforge.catalogue.adapter.out.persistence.entity.PhaseEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface PhaseR2dbcRepository extends ReactiveCrudRepository<PhaseEntity, UUID> {

    Flux<PhaseEntity> findByRoadmapIdOrderByPositionAsc(UUID roadmapId);
}
