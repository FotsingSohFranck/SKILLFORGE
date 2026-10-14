package dev.skillforge.gamification.adapter.out.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.skillforge.gamification.domain.model.Badge;
import dev.skillforge.gamification.domain.model.BadgeId;
import dev.skillforge.gamification.domain.port.out.BadgeRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;

@Component
public class BadgeRepositoryAdapter implements BadgeRepository {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final DatabaseClient db;

    public BadgeRepositoryAdapter(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Flux<Badge> findByRuleType(String ruleType) {
        return db.sql("SELECT id, code, name, rule_type, rule_config::text AS rule_config "
                        + "FROM badges WHERE rule_type = :type")
                .bind("type", ruleType)
                .map((row, meta) -> new Badge(
                        new BadgeId(row.get("id", UUID.class)),
                        row.get("code", String.class),
                        row.get("name", String.class),
                        row.get("rule_type", String.class),
                        parse(row.get("rule_config", String.class))))
                .all();
    }

    private static Map<String, Object> parse(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            throw new IllegalStateException("Lecture de rule_config impossible", e);
        }
    }
}
