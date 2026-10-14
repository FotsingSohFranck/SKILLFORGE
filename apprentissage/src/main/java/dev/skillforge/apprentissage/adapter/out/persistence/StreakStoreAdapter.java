package dev.skillforge.apprentissage.adapter.out.persistence;

import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.Streak;
import dev.skillforge.apprentissage.domain.port.out.StreakStore;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
public class StreakStoreAdapter implements StreakStore {

    private final DatabaseClient db;

    public StreakStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<Streak> find(LearnerId learner) {
        return db.sql("SELECT current_length, longest_length, last_activity_date "
                        + "FROM streaks WHERE user_id = :uid")
                .bind("uid", learner.value())
                .map((row, meta) -> new Streak(
                        row.get("current_length", Integer.class),
                        row.get("longest_length", Integer.class),
                        row.get("last_activity_date", LocalDate.class)))
                .one();
    }

    @Override
    public Mono<Void> save(LearnerId learner, Streak streak) {
        return db.sql("""
                        INSERT INTO streaks(user_id, current_length, longest_length, last_activity_date)
                        VALUES (:uid, :current, :longest, :date)
                        ON CONFLICT (user_id) DO UPDATE SET
                            current_length = EXCLUDED.current_length,
                            longest_length = EXCLUDED.longest_length,
                            last_activity_date = EXCLUDED.last_activity_date
                        """)
                .bind("uid", learner.value())
                .bind("current", streak.currentLength())
                .bind("longest", streak.longestLength())
                .bind("date", streak.lastActivityDate())
                .fetch().rowsUpdated().then();
    }
}
