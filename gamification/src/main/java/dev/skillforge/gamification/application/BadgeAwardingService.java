package dev.skillforge.gamification.application;

import dev.skillforge.gamification.domain.model.Badge;
import dev.skillforge.gamification.domain.model.LearnerId;
import dev.skillforge.gamification.domain.port.in.BadgeAwardingUseCase;
import dev.skillforge.gamification.domain.port.out.BadgeRepository;
import dev.skillforge.gamification.domain.port.out.EarnedBadgeStore;
import dev.skillforge.gamification.domain.port.out.XpLedger;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

/**
 * Évalue les règles de badges sur événements et attribue (idempotent). Règles simples
 * (compléter un nœud/parcours, réussir un examen), à seuil (streak N jours, XP N) via {@code ruleConfig}.
 */
public class BadgeAwardingService implements BadgeAwardingUseCase {

    private final BadgeRepository badges;
    private final EarnedBadgeStore earned;
    private final XpLedger ledger;

    public BadgeAwardingService(BadgeRepository badges, EarnedBadgeStore earned, XpLedger ledger) {
        this.badges = badges;
        this.earned = earned;
        this.ledger = ledger;
    }

    @Override
    public Mono<Void> onNodeCompleted(LearnerId learner) {
        return awardMatching("NODE_COMPLETED", learner, badge -> true);
    }

    @Override
    public Mono<Void> onRoadmapCompleted(LearnerId learner) {
        return awardMatching("ROADMAP_COMPLETED", learner, badge -> true);
    }

    @Override
    public Mono<Void> onExamPassed(LearnerId learner) {
        return awardMatching("EXAM_PASSED", learner, badge -> true);
    }

    @Override
    public Mono<Void> onStreak(LearnerId learner, int currentLength) {
        return awardMatching("STREAK_DAYS", learner, badge -> badge.intConfig("days") <= currentLength);
    }

    @Override
    public Mono<Void> onXpChanged(LearnerId learner) {
        return ledger.totalXp(learner)
                .flatMap(total -> awardMatching("XP_THRESHOLD", learner, badge -> badge.intConfig("xp") <= total));
    }

    private Mono<Void> awardMatching(String ruleType, LearnerId learner, Predicate<Badge> condition) {
        return badges.findByRuleType(ruleType)
                .filter(condition)
                .flatMap(badge -> earned.award(learner, badge.id()))
                .then();
    }
}
