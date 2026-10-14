package dev.skillforge.evaluation.adapter.out.persistence;

import dev.skillforge.evaluation.domain.model.ChallengeId;
import dev.skillforge.evaluation.domain.port.in.ChallengeImport;
import dev.skillforge.evaluation.domain.port.out.ChallengeImporter;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Adaptateur R2DBC : persistance idempotente d'un défi (upsert par {@code id}). */
@Component
public class ChallengeImporterAdapter implements ChallengeImporter {

    private final DatabaseClient db;

    public ChallengeImporterAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<ChallengeId> importChallenge(ChallengeImport c) {
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                        INSERT INTO code_challenges(id, node_id, lesson_id, title, statement,
                                                    skeleton, solution, key_points, language)
                        VALUES (:id, :node, :lesson, :title, :statement, :skeleton, :solution,
                                CAST(:keyPoints AS jsonb), :language)
                        ON CONFLICT (id) DO UPDATE SET
                            node_id = EXCLUDED.node_id, lesson_id = EXCLUDED.lesson_id,
                            title = EXCLUDED.title, statement = EXCLUDED.statement,
                            skeleton = EXCLUDED.skeleton, solution = EXCLUDED.solution,
                            key_points = EXCLUDED.key_points, language = EXCLUDED.language
                        """)
                .bind("id", c.id())
                .bind("title", c.title())
                .bind("statement", c.statement())
                .bind("solution", c.solution())
                .bind("keyPoints", JsonKeyPoints.toJson(c.keyPoints()));
        spec = bindNullable(spec, "node", c.nodeId(), UUID.class);
        spec = bindNullable(spec, "lesson", c.lessonId(), UUID.class);
        spec = bindNullable(spec, "skeleton", c.skeleton(), String.class);
        spec = bindNullable(spec, "language", c.language(), String.class);
        return spec.fetch().rowsUpdated().thenReturn(new ChallengeId(c.id()));
    }

    private static DatabaseClient.GenericExecuteSpec bindNullable(
            DatabaseClient.GenericExecuteSpec spec, String name, Object value, Class<?> type) {
        return value != null ? spec.bind(name, value) : spec.bindNull(name, type);
    }
}
