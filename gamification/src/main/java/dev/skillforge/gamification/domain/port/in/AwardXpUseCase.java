package dev.skillforge.gamification.domain.port.in;

import dev.skillforge.gamification.domain.model.LearnerId;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Attribuer de l'XP à un apprenant (inscription au grand livre). */
public interface AwardXpUseCase {
    Mono<Void> award(LearnerId learner, int amount, String reason, String sourceType, UUID sourceId);
}
