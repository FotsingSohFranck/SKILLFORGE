package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.Bookmark;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.port.in.ListBookmarksUseCase;
import dev.skillforge.apprentissage.domain.port.out.BookmarkStore;
import reactor.core.publisher.Flux;

public class ListBookmarksService implements ListBookmarksUseCase {

    private final BookmarkStore bookmarks;

    public ListBookmarksService(BookmarkStore bookmarks) {
        this.bookmarks = bookmarks;
    }

    @Override
    public Flux<Bookmark> list(LearnerId learner) {
        return bookmarks.findByLearner(learner);
    }
}
