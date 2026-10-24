package dev.skillforge.apprentissage.adapter.out.persistence;

import dev.skillforge.apprentissage.domain.model.Enrollment;
import dev.skillforge.apprentissage.domain.model.EnrollmentId;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import dev.skillforge.apprentissage.domain.port.out.EnrollmentStore;
import dev.skillforge.common.id.UuidV7;
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
public class EnrollmentStoreAdapter implements EnrollmentStore {

    private static final String SELECT =
            "SELECT id, user_id, roadmap_id, progress_percent, last_active_node_id, "
                    + "last_active_lesson_id, last_activity_at FROM enrollments ";

    private final DatabaseClient db;

    public EnrollmentStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<Enrollment> findByLearnerAndRoadmap(LearnerId learner, RoadmapId roadmap) {
        return db.sql(SELECT + "WHERE user_id = :uid AND roadmap_id = :rid")
                .bind("uid", learner.value()).bind("rid", roadmap.value())
                .map(EnrollmentStoreAdapter::map).one();
    }

    @Override
    public Flux<Enrollment> findByLearner(LearnerId learner) {
        return db.sql(SELECT + "WHERE user_id = :uid "
                        + "ORDER BY last_activity_at DESC NULLS LAST, id")
                .bind("uid", learner.value())
                .map(EnrollmentStoreAdapter::map).all();
    }

    @Override
    public Mono<Enrollment> create(LearnerId learner, RoadmapId roadmap) {
        UUID id = UuidV7.generate();
        return db.sql("INSERT INTO enrollments(id, user_id, roadmap_id) VALUES (:id, :uid, :rid) "
                        + "ON CONFLICT (user_id, roadmap_id) DO NOTHING")
                .bind("id", id).bind("uid", learner.value()).bind("rid", roadmap.value())
                .fetch().rowsUpdated()
                .then(findByLearnerAndRoadmap(learner, roadmap));
    }

    @Override
    public Mono<Void> updateProgress(EnrollmentId id, NodeId lastNode, LessonId lastLesson,
                                     double percent, Instant at) {
        return db.sql("UPDATE enrollments SET last_active_node_id = :node, "
                        + "last_active_lesson_id = :lesson, progress_percent = :percent, "
                        + "last_activity_at = :at WHERE id = :id")
                .bind("node", lastNode.value())
                .bind("lesson", lastLesson.value())
                .bind("percent", percent)
                .bind("at", OffsetDateTime.ofInstant(at, ZoneOffset.UTC))
                .bind("id", id.value())
                .fetch().rowsUpdated().then();
    }

    @Override
    public Mono<Void> updatePercent(EnrollmentId id, double percent, Instant at) {
        return db.sql("UPDATE enrollments SET progress_percent = :percent, last_activity_at = :at WHERE id = :id")
                .bind("percent", percent)
                .bind("at", OffsetDateTime.ofInstant(at, ZoneOffset.UTC))
                .bind("id", id.value())
                .fetch().rowsUpdated().then();
    }

    @Override
    public Mono<Enrollment> findLastActive(LearnerId learner) {
        return db.sql(SELECT + "WHERE user_id = :uid AND last_activity_at IS NOT NULL "
                        + "ORDER BY last_activity_at DESC LIMIT 1")
                .bind("uid", learner.value())
                .map(EnrollmentStoreAdapter::map).one();
    }

    private static Enrollment map(Readable row) {
        UUID node = row.get("last_active_node_id", UUID.class);
        UUID lesson = row.get("last_active_lesson_id", UUID.class);
        OffsetDateTime at = row.get("last_activity_at", OffsetDateTime.class);
        BigDecimal percent = row.get("progress_percent", BigDecimal.class);
        return new Enrollment(
                new EnrollmentId(row.get("id", UUID.class)),
                new LearnerId(row.get("user_id", UUID.class)),
                new RoadmapId(row.get("roadmap_id", UUID.class)),
                percent == null ? 0.0 : percent.doubleValue(),
                node == null ? null : new NodeId(node),
                lesson == null ? null : new LessonId(lesson),
                at == null ? null : at.toInstant());
    }
}
