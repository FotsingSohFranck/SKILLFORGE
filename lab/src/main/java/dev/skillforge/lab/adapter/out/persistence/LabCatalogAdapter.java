package dev.skillforge.lab.adapter.out.persistence;

import dev.skillforge.lab.domain.port.out.LabCatalogPort;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Lecture directe de {@code nodes.completion_policy} (couplage temporaire, cf. port). */
@Component
public class LabCatalogAdapter implements LabCatalogPort {

    private final DatabaseClient db;

    public LabCatalogAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<Boolean> requiresPractical(UUID nodeId) {
        return db.sql("SELECT completion_policy FROM nodes WHERE id = :nid")
                .bind("nid", nodeId)
                .map((row, meta) -> "LESSONS_AND_PRACTICAL".equals(row.get(0, String.class)))
                .one()
                .defaultIfEmpty(false);
    }
}
