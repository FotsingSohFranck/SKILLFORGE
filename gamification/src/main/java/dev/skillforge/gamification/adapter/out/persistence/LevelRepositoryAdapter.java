package dev.skillforge.gamification.adapter.out.persistence;

import dev.skillforge.gamification.domain.model.Level;
import dev.skillforge.gamification.domain.port.out.LevelRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class LevelRepositoryAdapter implements LevelRepository {

    private final DatabaseClient db;

    public LevelRepositoryAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Flux<Level> findAllOrdered() {
        return db.sql("SELECT code, name, min_xp, position FROM levels ORDER BY position ASC")
                .map((row, meta) -> new Level(
                        row.get("code", String.class),
                        row.get("name", String.class),
                        row.get("min_xp", Integer.class) == null ? 0 : row.get("min_xp", Integer.class),
                        row.get("position", Integer.class) == null ? 0 : row.get("position", Integer.class)))
                .all();
    }
}
