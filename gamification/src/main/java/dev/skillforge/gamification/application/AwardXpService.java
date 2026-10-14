package dev.skillforge.gamification.application;

import dev.skillforge.gamification.domain.model.LearnerId;
import dev.skillforge.gamification.domain.port.in.AwardXpUseCase;
import dev.skillforge.gamification.domain.port.out.XpLedger;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class AwardXpService implements AwardXpUseCase {

    private final XpLedger ledger;

    public AwardXpService(XpLedger ledger) {
        this.ledger = ledger;
    }

    @Override
    public Mono<Void> award(LearnerId learner, int amount, String reason, String sourceType, UUID sourceId) {
        if (amount == 0) {
            return Mono.empty();
        }
        return ledger.add(learner, amount, reason, sourceType, sourceId);
    }
}
