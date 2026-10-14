package dev.skillforge.apprentissage.domain.port.in;

import dev.skillforge.apprentissage.domain.model.BookmarkTarget;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Ajoute/retire un favori (bascule). Renvoie {@code true} si le favori est désormais actif. */
public interface ToggleBookmarkUseCase {
    Mono<Boolean> toggle(LearnerId learner, BookmarkTarget target, UUID targetId);
}
