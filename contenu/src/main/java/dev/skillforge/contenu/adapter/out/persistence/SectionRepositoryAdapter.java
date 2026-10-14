package dev.skillforge.contenu.adapter.out.persistence;

import dev.skillforge.contenu.adapter.out.persistence.mapper.ContentPersistenceMapper;
import dev.skillforge.contenu.adapter.out.persistence.repository.SectionR2dbcRepository;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.Section;
import dev.skillforge.contenu.domain.port.out.SectionRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class SectionRepositoryAdapter implements SectionRepository {

    private final SectionR2dbcRepository repository;

    public SectionRepositoryAdapter(SectionR2dbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<Section> findByLessonId(LessonId lesson) {
        return repository.findByLessonIdOrderByPositionAsc(lesson.value())
                .map(ContentPersistenceMapper::toDomain);
    }
}
