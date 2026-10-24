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
            "SELECT id, node_id, title, image, instructions, workspace_seed, validation_ref, timeout_seconds "
                    + "FROM practical_tasks ";

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

    private static PracticalTask map(Readable row) {
        return new PracticalTask(
                PracticalTaskId.of(row.get("id", UUID.class)),
                row.get("node_id", UUID.class),
                row.get("title", String.class),
                row.get("image", String.class),
                row.get("instructions", String.class),
                row.get("workspace_seed", String.class),
                row.get("validation_ref", String.class),
                row.get("timeout_seconds", Integer.class));
    }
}
