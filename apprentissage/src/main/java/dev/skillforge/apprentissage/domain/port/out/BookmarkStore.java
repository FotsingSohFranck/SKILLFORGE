package dev.skillforge.apprentissage.domain.port.out;

import dev.skillforge.apprentissage.domain.model.Bookmark;
import dev.skillforge.apprentissage.domain.model.BookmarkTarget;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Port sortant : persistance des favoris. */
public interface BookmarkStore {

    /** Bascule le favori ; renvoie {@code true} si présent après l'opération. */
    Mono<Boolean> toggle(LearnerId learner, BookmarkTarget target, UUID targetId);

    Flux<Bookmark> findByLearner(LearnerId learner);
}
