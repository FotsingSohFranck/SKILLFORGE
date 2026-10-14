package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.BookmarkTarget;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.port.in.ToggleBookmarkUseCase;
import dev.skillforge.apprentissage.domain.port.out.BookmarkStore;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class ToggleBookmarkService implements ToggleBookmarkUseCase {

    private final BookmarkStore bookmarks;

    public ToggleBookmarkService(BookmarkStore bookmarks) {
        this.bookmarks = bookmarks;
    }

    @Override
    public Mono<Boolean> toggle(LearnerId learner, BookmarkTarget target, UUID targetId) {
        return bookmarks.toggle(learner, target, targetId);
    }
}
