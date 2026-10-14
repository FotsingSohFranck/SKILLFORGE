package dev.skillforge.catalogue.adapter.out.persistence.repository;

import dev.skillforge.catalogue.adapter.out.persistence.entity.NodeEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface NodeR2dbcRepository extends ReactiveCrudRepository<NodeEntity, UUID> {

    Flux<NodeEntity> findByRoadmapIdOrderByPositionAsc(UUID roadmapId);
}
