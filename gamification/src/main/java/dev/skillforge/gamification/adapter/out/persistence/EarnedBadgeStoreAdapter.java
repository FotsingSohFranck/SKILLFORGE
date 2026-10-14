package dev.skillforge.gamification.adapter.out.persistence;

import dev.skillforge.gamification.domain.model.BadgeId;
import dev.skillforge.gamification.domain.model.EarnedBadge;
import dev.skillforge.gamification.domain.model.LearnerId;
import dev.skillforge.gamification.domain.port.out.EarnedBadgeStore;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Component
public class EarnedBadgeStoreAdapter implements EarnedBadgeStore {

    private final DatabaseClient db;

    public EarnedBadgeStoreAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<Void> award(LearnerId learner, BadgeId badge) {
        return db.sql("INSERT INTO earned_badges(user_id, badge_id) VALUES (:uid, :bid) "
                        + "ON CONFLICT (user_id, badge_id) DO NOTHING")
                .bind("uid", learner.value()).bind("bid", badge.value())
                .fetch().rowsUpdated().then();
    }

    @Override
    public Flux<EarnedBadge> findByLearner(LearnerId learner) {
        return db.sql("SELECT b.code, b.name, b.description, b.icon, eb.earned_at "
                        + "FROM earned_badges eb JOIN badges b ON b.id = eb.badge_id "
                        + "WHERE eb.user_id = :uid ORDER BY eb.earned_at DESC")
                .bind("uid", learner.value())
                .map((row, meta) -> {
                    OffsetDateTime earnedAt = row.get("earned_at", OffsetDateTime.class);
                    return new EarnedBadge(
                            row.get("code", String.class),
                            row.get("name", String.class),
                            row.get("description", String.class),
                            row.get("icon", String.class),
                            earnedAt == null ? null : earnedAt.toInstant());
                })
                .all();
    }
}
