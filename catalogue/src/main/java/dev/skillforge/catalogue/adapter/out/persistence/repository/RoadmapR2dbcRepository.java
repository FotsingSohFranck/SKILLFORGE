package dev.skillforge.catalogue.adapter.out.persistence.repository;

import dev.skillforge.catalogue.adapter.out.persistence.entity.RoadmapEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RoadmapR2dbcRepository extends ReactiveCrudRepository<RoadmapEntity, UUID> {

    Flux<RoadmapEntity> findAllByOrderByPositionAscTitleAsc();

    Mono<RoadmapEntity> findBySlug(String slug);

    @Query("""
            SELECT * FROM roadmaps
            WHERE title ILIKE :pattern OR slug ILIKE :pattern
            ORDER BY position ASC, title ASC
            """)
    Flux<RoadmapEntity> searchByPattern(@Param("pattern") String pattern);
}
