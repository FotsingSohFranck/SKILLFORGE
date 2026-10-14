package dev.skillforge.contenu.domain.port.out;

import dev.skillforge.contenu.domain.model.Lesson;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.NodeId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Port sortant : accès aux leçons. */
public interface LessonRepository {

    Flux<Lesson> findByNodeId(NodeId node);

    Mono<Lesson> findById(LessonId id);
}
