package dev.skillforge.contenu.adapter.out.persistence.repository;

import dev.skillforge.contenu.adapter.out.persistence.entity.LessonEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface LessonR2dbcRepository extends ReactiveCrudRepository<LessonEntity, UUID> {

    Flux<LessonEntity> findByNodeIdOrderByPositionAsc(UUID nodeId);
}
