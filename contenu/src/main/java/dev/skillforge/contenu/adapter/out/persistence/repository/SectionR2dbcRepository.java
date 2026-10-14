package dev.skillforge.contenu.adapter.out.persistence.repository;

import dev.skillforge.contenu.adapter.out.persistence.entity.SectionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface SectionR2dbcRepository extends ReactiveCrudRepository<SectionEntity, UUID> {

    Flux<SectionEntity> findByLessonIdOrderByPositionAsc(UUID lessonId);
}
