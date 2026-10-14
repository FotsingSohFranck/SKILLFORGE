package dev.skillforge.gamification.domain.port.out;

import dev.skillforge.gamification.domain.model.Badge;
import reactor.core.publisher.Flux;

/** Port sortant : définitions de badges par type de règle. */
public interface BadgeRepository {
    Flux<Badge> findByRuleType(String ruleType);
}
