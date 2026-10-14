package dev.skillforge.contenu.adapter.out.persistence;

import dev.skillforge.contenu.domain.model.Lesson;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.NodeId;
import dev.skillforge.contenu.domain.port.out.LessonWriteStore;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class LessonWriteStoreAdapter implements LessonWriteStore {

    private static final String RETURNING = " RETURNING id, node_id, slug, title, position";

    private final DatabaseClient db;

    public LessonWriteStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<Lesson> insert(NodeId node, String slug, String title, int position) {
        return db.sql("INSERT INTO lessons(node_id, slug, title, position) "
                        + "VALUES (:nid, :slug, :title, :position)" + RETURNING)
                .bind("nid", node.value()).bind("slug", slug).bind("title", title).bind("position", position)
                .map(LessonWriteStoreAdapter::map).one();
    }

    @Override
    public Mono<Lesson> update(LessonId lesson, String slug, String title, int position) {
        return db.sql("UPDATE lessons SET slug = :slug, title = :title, position = :position "
                        + "WHERE id = :id" + RETURNING)
                .bind("slug", slug).bind("title", title).bind("position", position)
                .bind("id", lesson.value())
                .map(LessonWriteStoreAdapter::map).one();
    }

    @Override
    public Mono<Long> delete(LessonId lesson) {
        return db.sql("DELETE FROM lessons WHERE id = :id")
                .bind("id", lesson.value()).fetch().rowsUpdated();
    }

    @Override
    public Mono<Void> updatePosition(LessonId lesson, int position) {
        return db.sql("UPDATE lessons SET position = :position WHERE id = :id")
                .bind("position", position).bind("id", lesson.value())
                .fetch().rowsUpdated().then();
    }

    private static Lesson map(Readable row) {
        Integer position = row.get("position", Integer.class);
        return new Lesson(
                new LessonId(row.get("id", UUID.class)),
                new NodeId(row.get("node_id", UUID.class)),
                row.get("slug", String.class),
                row.get("title", String.class),
                position == null ? 0 : position);
    }
}
