package dev.skillforge.apprentissage.adapter.out.persistence;

import dev.skillforge.apprentissage.domain.model.EnrollmentId;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.port.out.LessonProgressStore;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class LessonProgressStoreAdapter implements LessonProgressStore {

    private final DatabaseClient db;

    public LessonProgressStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<Void> markViewed(EnrollmentId enrollment, NodeId node, LessonId lesson, Instant at) {
        // Ne dégrade pas un statut COMPLETED existant (le statut n'est pas mis à jour au conflit).
        return db.sql("""
                        INSERT INTO lesson_progress(enrollment_id, node_id, lesson_id, status, viewed_at)
                        VALUES (:eid, :nid, :lid, 'VIEWED', :at)
                        ON CONFLICT (enrollment_id, lesson_id) DO UPDATE SET
                            viewed_at = EXCLUDED.viewed_at, node_id = EXCLUDED.node_id
                        """)
                .bind("eid", enrollment.value()).bind("nid", node.value()).bind("lid", lesson.value())
                .bind("at", OffsetDateTime.ofInstant(at, ZoneOffset.UTC))
                .fetch().rowsUpdated().then();
    }

    @Override
    public Mono<Void> markCompleted(EnrollmentId enrollment, NodeId node, LessonId lesson, Instant at) {
        return db.sql("""
                        INSERT INTO lesson_progress(enrollment_id, node_id, lesson_id, status, viewed_at, completed_at)
                        VALUES (:eid, :nid, :lid, 'COMPLETED', :at, :at)
                        ON CONFLICT (enrollment_id, lesson_id) DO UPDATE SET
                            status = 'COMPLETED', completed_at = EXCLUDED.completed_at
                        """)
                .bind("eid", enrollment.value()).bind("nid", node.value()).bind("lid", lesson.value())
                .bind("at", OffsetDateTime.ofInstant(at, ZoneOffset.UTC))
                .fetch().rowsUpdated().then();
    }

    @Override
    public Mono<Long> countCompletedInNode(EnrollmentId enrollment, NodeId node) {
        return db.sql("SELECT count(*) FROM lesson_progress "
                        + "WHERE enrollment_id = :eid AND node_id = :nid AND status = 'COMPLETED'")
                .bind("eid", enrollment.value()).bind("nid", node.value())
                .map((row, meta) -> row.get(0, Long.class)).one();
    }

    @Override
    public Flux<LessonId> findCompletedLessonIds(LearnerId learner, NodeId node) {
        // Jointure via l'inscription de l'apprenant : le nœud appartient à un seul parcours,
        // donc filtrer par user_id + node_id cible la bonne inscription sans besoin du roadmapId.
        return db.sql("""
                        SELECT lp.lesson_id
                        FROM lesson_progress lp
                        JOIN enrollments e ON e.id = lp.enrollment_id
                        WHERE e.user_id = :uid AND lp.node_id = :nid AND lp.status = 'COMPLETED'
                        """)
                .bind("uid", learner.value()).bind("nid", node.value())
                .map((row, meta) -> new LessonId(row.get("lesson_id", UUID.class)))
                .all();
    }
}
