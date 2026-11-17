package dev.skillforge.challenge.adapter.out.persistence;

import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import dev.skillforge.challenge.domain.model.ScenarioRef;
import dev.skillforge.challenge.domain.port.out.ScenarioStore;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adaptateur R2DBC : upsert du scénario (par {@code scenario_key}) puis insertion d'une nouvelle
 * version publiée (numéro = max + 1), définition stockée en {@code jsonb}. Lecture de la dernière
 * version publiée d'un nœud (définition relue en texte via {@code ::text}).
 */
@Component
public class ScenarioStoreAdapter implements ScenarioStore {

    private final DatabaseClient db;

    public ScenarioStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<ScenarioRef> publishNewVersion(ScenarioDefinition d) {
        String definitionJson = ScenarioDefinitionJson.toJson(d);
        return db.sql("""
                        INSERT INTO challenge_scenarios(scenario_key, node_id, title)
                        VALUES (:key, :node, :title)
                        ON CONFLICT (scenario_key) DO UPDATE SET
                            node_id = EXCLUDED.node_id, title = EXCLUDED.title, updated_at = now()
                        RETURNING id
                        """)
                .bind("key", d.scenarioKey())
                .bind("node", d.nodeId())
                .bind("title", d.title())
                .map((row, meta) -> row.get("id", UUID.class))
                .one()
                .flatMap(scenarioId -> db.sql("""
                                INSERT INTO challenge_scenario_versions
                                    (scenario_id, version, status, definition, published_at)
                                SELECT :sid, coalesce(max(version), 0) + 1, 'PUBLISHED',
                                       CAST(:def AS jsonb), now()
                                FROM challenge_scenario_versions WHERE scenario_id = :sid
                                RETURNING version
                                """)
                        .bind("sid", scenarioId)
                        .bind("def", definitionJson)
                        .map((row, meta) -> row.get("version", Integer.class))
                        .one()
                        .map(version -> new ScenarioRef(scenarioId, version)));
    }

    @Override
    public Mono<ScenarioDefinition> findLatestPublishedByNode(UUID nodeId) {
        return db.sql("""
                        SELECT v.definition::text AS definition
                        FROM challenge_scenarios s
                        JOIN challenge_scenario_versions v ON v.scenario_id = s.id
                        WHERE s.node_id = :node AND v.status = 'PUBLISHED'
                        ORDER BY v.version DESC
                        LIMIT 1
                        """)
                .bind("node", nodeId)
                .map((row, meta) -> row.get("definition", String.class))
                .one()
                .map(ScenarioDefinitionJson::fromJson);
    }

    @Override
    public Mono<ScenarioRef> findLatestPublishedRefByNode(UUID nodeId) {
        return db.sql("""
                        SELECT v.scenario_id AS scenario_id, v.version AS version
                        FROM challenge_scenarios s
                        JOIN challenge_scenario_versions v ON v.scenario_id = s.id
                        WHERE s.node_id = :node AND v.status = 'PUBLISHED'
                        ORDER BY v.version DESC
                        LIMIT 1
                        """)
                .bind("node", nodeId)
                .map((row, meta) -> new ScenarioRef(
                        row.get("scenario_id", UUID.class), row.get("version", Integer.class)))
                .one();
    }

    @Override
    public Mono<ScenarioDefinition> findByRef(UUID scenarioId, int version) {
        return db.sql("""
                        SELECT definition::text AS definition
                        FROM challenge_scenario_versions
                        WHERE scenario_id = :sid AND version = :ver
                        """)
                .bind("sid", scenarioId)
                .bind("ver", version)
                .map((row, meta) -> row.get("definition", String.class))
                .one()
                .map(ScenarioDefinitionJson::fromJson);
    }
}
