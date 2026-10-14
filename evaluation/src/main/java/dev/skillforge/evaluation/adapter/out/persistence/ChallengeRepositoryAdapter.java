package dev.skillforge.evaluation.adapter.out.persistence;

import dev.skillforge.evaluation.domain.model.ChallengeId;
import dev.skillforge.evaluation.domain.model.ChallengeSummary;
import dev.skillforge.evaluation.domain.model.CodeChallenge;
import dev.skillforge.evaluation.domain.port.out.ChallengeRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ChallengeRepositoryAdapter implements ChallengeRepository {

    private final DatabaseClient db;

    public ChallengeRepositoryAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<CodeChallenge> findById(ChallengeId challenge) {
        return db.sql("SELECT id, title, statement, skeleton, solution, key_points::text AS key_points, "
                        + "language FROM code_challenges WHERE id = :id")
                .bind("id", challenge.value())
                .map((row, meta) -> new CodeChallenge(
                        new ChallengeId(row.get("id", UUID.class)),
                        row.get("title", String.class),
                        row.get("statement", String.class),
                        row.get("skeleton", String.class),
                        row.get("solution", String.class),
                        JsonKeyPoints.fromJson(row.get("key_points", String.class)),
                        row.get("language", String.class)))
                .one();
    }

    @Override
    public Flux<ChallengeSummary> findByNode(UUID nodeId) {
        return db.sql("SELECT id, title, language FROM code_challenges "
                        + "WHERE node_id = :nid ORDER BY title")
                .bind("nid", nodeId)
                .map((row, meta) -> new ChallengeSummary(
                        new ChallengeId(row.get("id", UUID.class)),
                        row.get("title", String.class),
                        row.get("language", String.class)))
                .all();
    }
}
