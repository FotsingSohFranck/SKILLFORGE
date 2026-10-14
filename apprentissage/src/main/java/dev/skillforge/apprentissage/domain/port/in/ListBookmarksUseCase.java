package dev.skillforge.apprentissage.domain.port.in;

import dev.skillforge.apprentissage.domain.model.Bookmark;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import reactor.core.publisher.Flux;

/** Liste les favoris d'un apprenant. */
public interface ListBookmarksUseCase {
    Flux<Bookmark> list(LearnerId learner);
}
