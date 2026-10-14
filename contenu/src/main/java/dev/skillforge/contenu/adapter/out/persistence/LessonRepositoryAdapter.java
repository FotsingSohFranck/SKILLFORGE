package dev.skillforge.contenu.adapter.out.persistence;

import dev.skillforge.contenu.adapter.out.persistence.mapper.ContentPersistenceMapper;
import dev.skillforge.contenu.adapter.out.persistence.repository.LessonR2dbcRepository;
import dev.skillforge.contenu.domain.model.Lesson;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.NodeId;
import dev.skillforge.contenu.domain.port.out.LessonRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class LessonRepositoryAdapter implements LessonRepository {

    private final LessonR2dbcRepository repository;

    public LessonRepositoryAdapter(LessonR2dbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<Lesson> findByNodeId(NodeId node) {
        return repository.findByNodeIdOrderByPositionAsc(node.value())
                .map(ContentPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Lesson> findById(LessonId id) {
        return repository.findById(id.value()).map(ContentPersistenceMapper::toDomain);
    }
}
