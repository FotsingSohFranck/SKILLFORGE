package dev.skillforge.apprentissage.adapter.out.persistence;

import dev.skillforge.apprentissage.domain.model.EnrollmentId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.NodeProgress;
import dev.skillforge.apprentissage.domain.model.ProgressStatus;
import dev.skillforge.apprentissage.domain.port.out.NodeProgressStore;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class NodeProgressStoreAdapter implements NodeProgressStore {

    private final DatabaseClient db;

    public NodeProgressStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<Void> upsert(EnrollmentId enrollment, NodeId node, ProgressStatus status,
                             double percent, LessonId lastLesson, Instant at) {
        return db.sql("""
                        INSERT INTO node_progress(enrollment_id, node_id, status, percent, last_lesson_id, last_viewed_at)
                        VALUES (:eid, :nid, :status, :percent, :lesson, :at)
                        ON CONFLICT (enrollment_id, node_id) DO UPDATE SET
                            status = EXCLUDED.status, percent = EXCLUDED.percent,
                            last_lesson_id = EXCLUDED.last_lesson_id, last_viewed_at = EXCLUDED.last_viewed_at
                        """)
                .bind("eid", enrollment.value()).bind("nid", node.value())
                .bind("status", status.name()).bind("percent", percent)
                .bind("lesson", lastLesson.value())
                .bind("at", OffsetDateTime.ofInstant(at, ZoneOffset.UTC))
                .fetch().rowsUpdated().then();
    }

    @Override
    public Flux<NodeProgress> findByEnrollment(EnrollmentId enrollment) {
        return db.sql("SELECT node_id, status, percent, last_lesson_id "
                        + "FROM node_progress WHERE enrollment_id = :eid")
                .bind("eid", enrollment.value())
                .map(NodeProgressStoreAdapter::map).all();
    }

    @Override
    public Mono<Long> countCompleted(EnrollmentId enrollment) {
        return db.sql("SELECT count(*) FROM node_progress WHERE enrollment_id = :eid AND status = 'COMPLETED'")
                .bind("eid", enrollment.value())
                .map((row, meta) -> row.get(0, Long.class)).one();
    }

    private static NodeProgress map(Readable row) {
        UUID lesson = row.get("last_lesson_id", UUID.class);
        BigDecimal percent = row.get("percent", BigDecimal.class);
        return new NodeProgress(
                new NodeId(row.get("node_id", UUID.class)),
                ProgressStatus.valueOf(row.get("status", String.class)),
                percent == null ? 0.0 : percent.doubleValue(),
                lesson == null ? null : new LessonId(lesson));
    }
}
