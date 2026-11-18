package dev.skillforge.challenge.adapter.out.persistence;

import dev.skillforge.challenge.domain.model.AttemptStatus;
import dev.skillforge.challenge.domain.model.ChallengeAttempt;
import dev.skillforge.challenge.domain.model.ChallengeResult;
import dev.skillforge.challenge.domain.model.Decision;
import dev.skillforge.challenge.domain.model.ScenarioRef;
import dev.skillforge.challenge.domain.model.SimulationState;
import dev.skillforge.challenge.domain.port.out.AttemptStore;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Adaptateur R2DBC des tentatives : état de simulation et rapport stockés en {@code jsonb}, journal
 * des décisions append-only. L'écriture de la décision et la mise à jour de la tentative sont
 * enchaînées (une décision par appel ; file formatif mono-apprenant).
 */
// Nom de bean explicite : évite la collision avec evaluation...AttemptStoreAdapter
// (même nom simple) lors du scan du contexte applicatif complet.
@Component("challengeAttemptStoreAdapter")
public class AttemptStoreAdapter implements AttemptStore {

    private final DatabaseClient db;

    public AttemptStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<ChallengeAttempt> create(UUID scenarioId, int version, UUID nodeId, UUID learnerId,
                                         String currentNodeId, SimulationState initialState) {
        return db.sql("""
                        INSERT INTO challenge_attempts
                            (scenario_id, scenario_version, node_id, learner_id, current_node_id, state)
                        VALUES (:sid, :ver, :node, :learner, :cur, CAST(:state AS jsonb))
                        RETURNING id
                        """)
                .bind("sid", scenarioId)
                .bind("ver", version)
                .bind("node", nodeId)
                .bind("learner", learnerId)
                .bind("cur", currentNodeId)
                .bind("state", ChallengeJson.write(initialState))
                .map((row, meta) -> row.get("id", UUID.class))
                .one()
                .map(id -> new ChallengeAttempt(id, new ScenarioRef(scenarioId, version), nodeId, learnerId,
                        AttemptStatus.IN_PROGRESS, currentNodeId, initialState, List.of(), null));
    }

    private static final String SELECT_COLUMNS = """
            SELECT id, scenario_id, scenario_version, node_id, learner_id, status,
                   current_node_id, state::text AS state, result::text AS result
            FROM challenge_attempts
            """;

    @Override
    public Mono<ChallengeAttempt> findOwned(UUID attemptId, UUID learnerId) {
        return db.sql(SELECT_COLUMNS + " WHERE id = :id AND learner_id = :learner")
                .bind("id", attemptId)
                .bind("learner", learnerId)
                .map((row, meta) -> mapRow(row))
                .one()
                .flatMap(this::hydrate);
    }

    @Override
    public Mono<ChallengeAttempt> findInProgressByLearnerAndNode(UUID learnerId, UUID nodeId) {
        return db.sql(SELECT_COLUMNS
                        + " WHERE learner_id = :learner AND node_id = :node AND status = 'IN_PROGRESS'"
                        + " ORDER BY started_at DESC LIMIT 1")
                .bind("learner", learnerId)
                .bind("node", nodeId)
                .map((row, meta) -> mapRow(row))
                .one()
                .flatMap(this::hydrate);
    }

    private Mono<ChallengeAttempt> hydrate(AttemptRow r) {
        return decisions(r.id()).map(decisions -> new ChallengeAttempt(
                r.id(), new ScenarioRef(r.scenarioId(), r.version()), r.nodeId(), r.learnerId(),
                r.status(), r.currentNodeId(),
                ChallengeJson.read(r.state(), SimulationState.class),
                decisions,
                r.result() == null ? null : ChallengeJson.read(r.result(), ChallengeResult.class)));
    }

    private static AttemptRow mapRow(io.r2dbc.spi.Readable row) {
        return new AttemptRow(
                row.get("id", UUID.class),
                row.get("scenario_id", UUID.class),
                row.get("scenario_version", Integer.class),
                row.get("node_id", UUID.class),
                row.get("learner_id", UUID.class),
                AttemptStatus.valueOf(row.get("status", String.class)),
                row.get("current_node_id", String.class),
                row.get("state", String.class),
                row.get("result", String.class));
    }

    private Mono<List<Decision>> decisions(UUID attemptId) {
        return db.sql("""
                        SELECT seq, node_id, choice_id
                        FROM challenge_decisions
                        WHERE attempt_id = :id
                        ORDER BY seq
                        """)
                .bind("id", attemptId)
                .map((row, meta) -> new Decision(
                        row.get("seq", Integer.class),
                        row.get("node_id", String.class),
                        row.get("choice_id", String.class)))
                .all()
                .collectList();
    }

    @Override
    public Mono<Void> advance(UUID attemptId, Decision decision, SimulationState state, String currentNodeId) {
        return insertDecision(attemptId, decision)
                .then(db.sql("""
                                UPDATE challenge_attempts
                                SET state = CAST(:state AS jsonb), current_node_id = :cur
                                WHERE id = :id
                                """)
                        .bind("state", ChallengeJson.write(state))
                        .bind("cur", currentNodeId)
                        .bind("id", attemptId)
                        .fetch().rowsUpdated())
                .then();
    }

    @Override
    public Mono<Void> complete(UUID attemptId, Decision decision, SimulationState finalState, ChallengeResult result) {
        return insertDecision(attemptId, decision)
                .then(db.sql("""
                                UPDATE challenge_attempts
                                SET status = 'COMPLETED', state = CAST(:state AS jsonb),
                                    global_score = :gs, passed = :passed,
                                    result = CAST(:result AS jsonb), completed_at = now()
                                WHERE id = :id
                                """)
                        .bind("state", ChallengeJson.write(finalState))
                        .bind("gs", result.globalScore())
                        .bind("passed", result.passed())
                        .bind("result", ChallengeJson.write(result))
                        .bind("id", attemptId)
                        .fetch().rowsUpdated())
                .then();
    }

    private Mono<Long> insertDecision(UUID attemptId, Decision decision) {
        return db.sql("""
                        INSERT INTO challenge_decisions (attempt_id, seq, node_id, choice_id)
                        VALUES (:id, :seq, :node, :choice)
                        """)
                .bind("id", attemptId)
                .bind("seq", decision.seq())
                .bind("node", decision.nodeId())
                .bind("choice", decision.choiceId())
                .fetch().rowsUpdated();
    }

    private record AttemptRow(UUID id, UUID scenarioId, int version, UUID nodeId, UUID learnerId,
                              AttemptStatus status, String currentNodeId, String state, String result) {
    }
}
