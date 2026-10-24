package dev.skillforge.lab.adapter.out.persistence;

import dev.skillforge.lab.domain.model.PracticalTask;
import dev.skillforge.lab.domain.model.PracticalTaskId;
import dev.skillforge.lab.domain.port.out.PracticalTaskStore;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class PracticalTaskStoreAdapter implements PracticalTaskStore {

    private static final String SELECT =
            "SELECT id, node_id, title, image, instructions, workspace_seed, validation_ref, timeout_seconds, "
                    + "expected_path FROM practical_tasks ";

    private final DatabaseClient db;

    public PracticalTaskStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<PracticalTask> findByNode(UUID nodeId) {
        return db.sql(SELECT + "WHERE node_id = :n")
                .bind("n", nodeId)
                .map(PracticalTaskStoreAdapter::map)
                .one();
    }

    @Override
    public Mono<PracticalTask> findById(PracticalTaskId id) {
        return db.sql(SELECT + "WHERE id = :id")
                .bind("id", id.value())
                .map(PracticalTaskStoreAdapter::map)
                .one();
    }

    @Override
    public Mono<Void> upsert(PracticalTask task) {
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                        INSERT INTO practical_tasks
                            (id, node_id, title, image, instructions, workspace_seed, validation_ref,
                             timeout_seconds, expected_path)
                        VALUES (:id, :node, :title, :image, :instructions, :seed, :ref, :timeout, :epath)
                        ON CONFLICT (node_id) DO UPDATE SET
                            title = EXCLUDED.title,
                            image = EXCLUDED.image,
                            instructions = EXCLUDED.instructions,
                            workspace_seed = EXCLUDED.workspace_seed,
                            validation_ref = EXCLUDED.validation_ref,
                            timeout_seconds = EXCLUDED.timeout_seconds,
                            expected_path = EXCLUDED.expected_path,
                            updated_at = now()
                        """)
                .bind("id", task.id().value())
                .bind("node", task.nodeId())
                .bind("title", task.title())
                .bind("image", task.image())
                .bind("instructions", task.instructions())
                .bind("ref", task.validationRef())
                .bind("timeout", task.timeoutSeconds());
        spec = task.workspaceSeed() == null
                ? spec.bindNull("seed", String.class)
                : spec.bind("seed", task.workspaceSeed());
        spec = task.expectedPath() == null
                ? spec.bindNull("epath", String.class)
                : spec.bind("epath", task.expectedPath());
        return spec.fetch().rowsUpdated().then();
    }

    private static PracticalTask map(Readable row) {
        return new PracticalTask(
                PracticalTaskId.of(row.get("id", UUID.class)),
                row.get("node_id", UUID.class),
                row.get("title", String.class),
                row.get("image", String.class),
                row.get("instructions", String.class),
                row.get("workspace_seed", String.class),
                row.get("validation_ref", String.class),
                row.get("timeout_seconds", Integer.class),
                row.get("expected_path", String.class));
    }
}
