package dev.skillforge.gamification.application;

import dev.skillforge.gamification.domain.model.Badge;
import dev.skillforge.gamification.domain.model.BadgeId;
import dev.skillforge.gamification.domain.model.EarnedBadge;
import dev.skillforge.gamification.domain.model.LearnerId;
import dev.skillforge.gamification.domain.port.out.BadgeRepository;
import dev.skillforge.gamification.domain.port.out.EarnedBadgeStore;
import dev.skillforge.gamification.domain.port.out.XpLedger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BadgeAwardingServiceTest {

    private final BadgeId nodeMaster = new BadgeId(UUID.randomUUID());
    private final BadgeId streak7 = new BadgeId(UUID.randomUUID());
    private final BadgeId xp1000 = new BadgeId(UUID.randomUUID());
    private final LearnerId learner = new LearnerId(UUID.randomUUID());
    private final Set<BadgeId> awarded = new HashSet<>();

    private final BadgeRepository badges = ruleType -> switch (ruleType) {
        case "NODE_COMPLETED" -> Flux.just(new Badge(nodeMaster, "NODE_MASTER", "n", "NODE_COMPLETED", Map.of()));
        case "STREAK_DAYS" -> Flux.just(new Badge(streak7, "STREAK_7", "s", "STREAK_DAYS", Map.of("days", 7)));
        case "XP_THRESHOLD" -> Flux.just(new Badge(xp1000, "XP_1000", "x", "XP_THRESHOLD", Map.of("xp", 1000)));
        default -> Flux.empty();
    };
    private final EarnedBadgeStore earned = new EarnedBadgeStore() {
        @Override
        public Mono<Void> award(LearnerId l, BadgeId badge) {
            awarded.add(badge);
            return Mono.empty();
        }

        @Override
        public Flux<EarnedBadge> findByLearner(LearnerId l) {
            return Flux.empty();
        }
    };

    private BadgeAwardingService service(long totalXp) {
        XpLedger ledger = new XpLedger() {
            @Override
            public Mono<Void> add(LearnerId l, int a, String r, String t, UUID s) {
                return Mono.empty();
            }

            @Override
            public Mono<Long> totalXp(LearnerId l) {
                return Mono.just(totalXp);
            }
        };
        return new BadgeAwardingService(badges, earned, ledger);
    }

    @Test
    void noeud_complete_attribue_NODE_MASTER() {
        service(0).onNodeCompleted(learner).block();
        assertTrue(awarded.contains(nodeMaster));
    }

    @Test
    void streak_attribue_selon_le_seuil_de_jours() {
        service(0).onStreak(learner, 10).block();       // 10 >= 7
        assertTrue(awarded.contains(streak7));
        awarded.clear();
        service(0).onStreak(learner, 3).block();        // 3 < 7
        assertFalse(awarded.contains(streak7));
    }

    @Test
    void xp_threshold_attribue_au_dela_du_seuil() {
        service(500).onXpChanged(learner).block();      // 500 < 1000
        assertFalse(awarded.contains(xp1000));
        service(1500).onXpChanged(learner).block();     // 1500 >= 1000
        assertTrue(awarded.contains(xp1000));
    }
}
