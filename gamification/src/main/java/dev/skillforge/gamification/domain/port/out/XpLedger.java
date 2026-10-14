package dev.skillforge.gamification.domain.port.out;

import dev.skillforge.gamification.domain.model.LearnerId;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Port sortant : grand livre d'XP (append-only) et total cumulé. */
public interface XpLedger {

    Mono<Void> add(LearnerId learner, int amount, String reason, String sourceType, UUID sourceId);

    Mono<Long> totalXp(LearnerId learner);
}
