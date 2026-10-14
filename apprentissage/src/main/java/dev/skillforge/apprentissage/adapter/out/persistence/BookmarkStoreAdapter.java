package dev.skillforge.apprentissage.adapter.out.persistence;

import dev.skillforge.apprentissage.domain.model.Bookmark;
import dev.skillforge.apprentissage.domain.model.BookmarkTarget;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.port.out.BookmarkStore;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class BookmarkStoreAdapter implements BookmarkStore {

    private final DatabaseClient db;

    public BookmarkStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<Boolean> toggle(LearnerId learner, BookmarkTarget target, UUID targetId) {
        return db.sql("DELETE FROM bookmarks WHERE user_id = :uid AND target_type = :type "
                        + "AND target_id = :tid RETURNING id")
                .bind("uid", learner.value()).bind("type", target.name()).bind("tid", targetId)
                .map((row, meta) -> row.get("id", UUID.class)).one()
                .map(existing -> Boolean.FALSE) // existait -> retiré
                .switchIfEmpty(Mono.defer(() -> db.sql(
                                "INSERT INTO bookmarks(user_id, target_type, target_id) VALUES (:uid, :type, :tid)")
                        .bind("uid", learner.value()).bind("type", target.name()).bind("tid", targetId)
                        .fetch().rowsUpdated().thenReturn(Boolean.TRUE)));
    }

    @Override
    public Flux<Bookmark> findByLearner(LearnerId learner) {
        return db.sql("SELECT target_type, target_id FROM bookmarks WHERE user_id = :uid "
                        + "ORDER BY created_at DESC")
                .bind("uid", learner.value())
                .map((row, meta) -> new Bookmark(
                        BookmarkTarget.valueOf(row.get("target_type", String.class)),
                        row.get("target_id", UUID.class)))
                .all();
    }
}
