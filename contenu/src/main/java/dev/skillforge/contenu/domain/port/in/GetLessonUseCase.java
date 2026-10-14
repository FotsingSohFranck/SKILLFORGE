package dev.skillforge.contenu.domain.port.in;

import dev.skillforge.contenu.domain.model.LessonDetail;
import dev.skillforge.contenu.domain.model.LessonId;
import reactor.core.publisher.Mono;

/** Obtenir une leçon et ses sections (404 si absente). */
public interface GetLessonUseCase {
    Mono<LessonDetail> get(LessonId lesson);
}
