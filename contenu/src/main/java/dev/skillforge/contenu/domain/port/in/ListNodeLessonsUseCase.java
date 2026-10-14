package dev.skillforge.contenu.domain.port.in;

import dev.skillforge.contenu.domain.model.Lesson;
import dev.skillforge.contenu.domain.model.NodeId;
import reactor.core.publisher.Flux;

/** Lister les leçons d'un nœud, ordonnées. */
public interface ListNodeLessonsUseCase {
    Flux<Lesson> list(NodeId node);
}
