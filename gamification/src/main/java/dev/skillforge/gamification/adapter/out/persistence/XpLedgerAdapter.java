package dev.skillforge.gamification.adapter.out.persistence;

import dev.skillforge.gamification.domain.model.LearnerId;
import dev.skillforge.gamification.domain.port.out.XpLedger;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class XpLedgerAdapter implements XpLedger {

    private final DatabaseClient db;

    public XpLedgerAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<Void> add(LearnerId learner, int amount, String reason, String sourceType, UUID sourceId) {
        DatabaseClient.GenericExecuteSpec spec = db.sql("""
                        INSERT INTO xp_ledger(user_id, amount, reason, source_type, source_id)
                        VALUES (:uid, :amount, :reason, :type, :source)
                        """)
                .bind("uid", learner.value()).bind("amount", amount).bind("reason", reason);
        spec = sourceType != null ? spec.bind("type", sourceType) : spec.bindNull("type", String.class);
        spec = sourceId != null ? spec.bind("source", sourceId) : spec.bindNull("source", UUID.class);
        return spec.fetch().rowsUpdated().then();
    }

    @Override
    public Mono<Long> totalXp(LearnerId learner) {
        return db.sql("SELECT COALESCE(SUM(amount), 0) FROM xp_ledger WHERE user_id = :uid")
                .bind("uid", learner.value())
                .map((row, meta) -> row.get(0, Long.class)).one();
    }
}
