package dev.skillforge.catalogue.adapter.out.persistence.repository;

import dev.skillforge.catalogue.adapter.out.persistence.entity.EdgeEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface EdgeR2dbcRepository extends ReactiveCrudRepository<EdgeEntity, UUID> {

    Flux<EdgeEntity> findByRoadmapId(UUID roadmapId);
}
