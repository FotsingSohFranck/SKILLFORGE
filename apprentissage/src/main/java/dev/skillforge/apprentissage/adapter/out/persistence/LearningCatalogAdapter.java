package dev.skillforge.apprentissage.adapter.out.persistence;

import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import dev.skillforge.apprentissage.domain.port.out.LearningCatalogPort;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Implémentation TEMPORAIRE (SF-21) du port {@link LearningCatalogPort} : lecture directe des
 * tables {@code lessons} et {@code nodes}. À remplacer par un service exposé par les contextes
 * {@code contenu}/{@code catalogue} en SF-22 (cf. dette dans BACKLOG.md).
 */
@Component
public class LearningCatalogAdapter implements LearningCatalogPort {

    private final DatabaseClient db;

    public LearningCatalogAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<Long> countLessonsInNode(NodeId node) {
        return db.sql("SELECT count(*) FROM lessons WHERE node_id = :nid")
                .bind("nid", node.value())
                .map((row, meta) -> row.get(0, Long.class)).one();
    }

    @Override
    public Mono<Long> countNodesInRoadmap(RoadmapId roadmap) {
        return db.sql("SELECT count(*) FROM nodes WHERE roadmap_id = :rid")
                .bind("rid", roadmap.value())
                .map((row, meta) -> row.get(0, Long.class)).one();
    }
}
