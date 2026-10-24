package dev.skillforge.lab.adapter.out.persistence;

import dev.skillforge.lab.domain.model.AttemptStatus;
import dev.skillforge.lab.domain.model.LearnerId;
import dev.skillforge.lab.domain.model.PracticalAttempt;
import dev.skillforge.lab.domain.model.PracticalTaskId;
import dev.skillforge.lab.domain.port.out.PracticalAttemptStore;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class PracticalAttemptStoreAdapter implements PracticalAttemptStore {

    private static final String SELECT =
            "SELECT id, user_id, practical_task_id, node_id, status, passed, output, started_at, finished_at "
                    + "FROM practical_attempts ";

    private final DatabaseClient db;

    public PracticalAttemptStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<PracticalAttempt> save(PracticalAttempt a) {
        return db.sql("INSERT INTO practical_attempts"
                        + "(id, user_id, practical_task_id, node_id, status, passed, output, started_at, finished_at) "
                        + "VALUES (:id, :uid, :tid, :nid, :status, :passed, :output, :started, :finished) "
                        + "ON CONFLICT (id) DO UPDATE SET "
                        + "status = EXCLUDED.status, passed = EXCLUDED.passed, output = EXCLUDED.output, "
                        + "finished_at = EXCLUDED.finished_at")
                .bind("id", a.id())
                .bind("uid", a.learner().value())
                .bind("tid", a.taskId().value())
                .bind("nid", a.nodeId())
                .bind("status", a.status().name())
                .bind("passed", a.passed())
                .bind("output", a.output() == null ? "" : a.output())
                .bind("started", offset(a.startedAt()))
                .bind("finished", a.finishedAt() == null ? null : offset(a.finishedAt()))
                .fetch().rowsUpdated()
                .thenReturn(a);
    }

    @Override
    public Mono<PracticalAttempt> findLatest(LearnerId learner, UUID nodeId) {
        return db.sql(SELECT + "WHERE user_id = :uid AND node_id = :nid ORDER BY started_at DESC LIMIT 1")
                .bind("uid", learner.value())
                .bind("nid", nodeId)
                .map(PracticalAttemptStoreAdapter::map)
                .one();
    }

    @Override
    public Mono<Boolean> hasPassed(LearnerId learner, UUID nodeId) {
        return db.sql("SELECT EXISTS(SELECT 1 FROM practical_attempts "
                        + "WHERE user_id = :uid AND node_id = :nid AND passed = true) AS ok")
                .bind("uid", learner.value())
                .bind("nid", nodeId)
                .map(row -> Boolean.TRUE.equals(row.get("ok", Boolean.class)))
                .one();
    }

    private static PracticalAttempt map(Readable row) {
        OffsetDateTime finished = row.get("finished_at", OffsetDateTime.class);
        return new PracticalAttempt(
                row.get("id", UUID.class),
                LearnerId.of(row.get("user_id", UUID.class)),
                PracticalTaskId.of(row.get("practical_task_id", UUID.class)),
                row.get("node_id", UUID.class),
                AttemptStatus.valueOf(row.get("status", String.class)),
                row.get("output", String.class),
                row.get("started_at", OffsetDateTime.class).toInstant(),
                finished == null ? null : finished.toInstant());
    }

    private static OffsetDateTime offset(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
