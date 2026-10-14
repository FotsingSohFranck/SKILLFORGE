package dev.skillforge.evaluation.adapter.out.persistence;

import dev.skillforge.common.id.UuidV7;
import dev.skillforge.evaluation.domain.model.DomainResult;
import dev.skillforge.evaluation.domain.model.ExamAttempt;
import dev.skillforge.evaluation.domain.model.ExamAttemptId;
import dev.skillforge.evaluation.domain.model.ExamDomainId;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.port.out.ExamAttemptStore;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Component
public class ExamAttemptStoreAdapter implements ExamAttemptStore {

    private static final String SELECT_ATTEMPT =
            "SELECT id, user_id, mock_exam_id, status, started_at, deadline_at, submitted_at, "
                    + "total_score, max_score, passed FROM exam_attempts ";

    private final DatabaseClient db;

    public ExamAttemptStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<ExamAttempt> start(LearnerId learner, ExamId exam, Instant startedAt, Instant deadlineAt) {
        UUID id = UuidV7.generate();
        return db.sql("INSERT INTO exam_attempts(id, user_id, mock_exam_id, status, started_at, deadline_at) "
                        + "VALUES (:id, :uid, :eid, 'IN_PROGRESS', :start, :deadline)")
                .bind("id", id).bind("uid", learner.value()).bind("eid", exam.value())
                .bind("start", offset(startedAt)).bind("deadline", offset(deadlineAt))
                .fetch().rowsUpdated()
                .thenReturn(new ExamAttempt(new ExamAttemptId(id), exam, learner,
                        startedAt, deadlineAt, null, 0, 0, null, "IN_PROGRESS"));
    }

    @Override
    public Mono<ExamAttempt> findById(ExamAttemptId attempt) {
        return db.sql(SELECT_ATTEMPT + "WHERE id = :id")
                .bind("id", attempt.value())
                .map(ExamAttemptStoreAdapter::map).one();
    }

    @Override
    @Transactional
    public Mono<Void> complete(ExamAttemptId attempt, Instant submittedAt, double totalScore,
                               double maxScore, boolean passed, String status, List<DomainResult> domains) {
        return db.sql("UPDATE exam_attempts SET submitted_at = :sub, total_score = :total, "
                        + "max_score = :max, passed = :passed, status = :status WHERE id = :id")
                .bind("sub", offset(submittedAt)).bind("total", totalScore).bind("max", maxScore)
                .bind("passed", passed).bind("status", status).bind("id", attempt.value())
                .fetch().rowsUpdated()
                .then(db.sql("DELETE FROM exam_domain_results WHERE exam_attempt_id = :id")
                        .bind("id", attempt.value()).fetch().rowsUpdated())
                .thenMany(Flux.fromIterable(domains))
                .concatMap(domain -> db.sql("""
                                INSERT INTO exam_domain_results(exam_attempt_id, exam_domain_id, score, max_score, is_weak)
                                VALUES (:aid, :did, :score, :max, :weak)
                                """)
                        .bind("aid", attempt.value()).bind("did", domain.domainId().value())
                        .bind("score", domain.score()).bind("max", domain.maxScore())
                        .bind("weak", domain.weak())
                        .fetch().rowsUpdated())
                .then();
    }

    @Override
    public Flux<ExamAttempt> history(LearnerId learner, ExamId exam) {
        return db.sql(SELECT_ATTEMPT + "WHERE user_id = :uid AND mock_exam_id = :eid ORDER BY started_at DESC")
                .bind("uid", learner.value()).bind("eid", exam.value())
                .map(ExamAttemptStoreAdapter::map).all();
    }

    @Override
    public Mono<ExamAttempt> lastSubmitted(LearnerId learner, ExamId exam) {
        return db.sql(SELECT_ATTEMPT + "WHERE user_id = :uid AND mock_exam_id = :eid "
                        + "AND submitted_at IS NOT NULL ORDER BY submitted_at DESC LIMIT 1")
                .bind("uid", learner.value()).bind("eid", exam.value())
                .map(ExamAttemptStoreAdapter::map).one();
    }

    @Override
    public Flux<DomainResult> domainResults(ExamAttemptId attempt) {
        return db.sql("SELECT exam_domain_id, score, max_score, is_weak "
                        + "FROM exam_domain_results WHERE exam_attempt_id = :id")
                .bind("id", attempt.value())
                .map((row, meta) -> new DomainResult(
                        new ExamDomainId(row.get("exam_domain_id", UUID.class)),
                        toDouble(row.get("score", BigDecimal.class)),
                        toDouble(row.get("max_score", BigDecimal.class)),
                        Boolean.TRUE.equals(row.get("is_weak", Boolean.class))))
                .all();
    }

    private static ExamAttempt map(Readable row) {
        OffsetDateTime deadline = row.get("deadline_at", OffsetDateTime.class);
        OffsetDateTime submitted = row.get("submitted_at", OffsetDateTime.class);
        OffsetDateTime started = row.get("started_at", OffsetDateTime.class);
        return new ExamAttempt(
                new ExamAttemptId(row.get("id", UUID.class)),
                new ExamId(row.get("mock_exam_id", UUID.class)),
                new LearnerId(row.get("user_id", UUID.class)),
                started == null ? null : started.toInstant(),
                deadline == null ? null : deadline.toInstant(),
                submitted == null ? null : submitted.toInstant(),
                toDouble(row.get("total_score", BigDecimal.class)),
                toDouble(row.get("max_score", BigDecimal.class)),
                row.get("passed", Boolean.class),
                row.get("status", String.class));
    }

    private static OffsetDateTime offset(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static double toDouble(BigDecimal v) {
        return v == null ? 0.0 : v.doubleValue();
    }
}
