package dev.skillforge.gamification.config;

import dev.skillforge.gamification.application.AwardXpService;
import dev.skillforge.gamification.application.BadgeAwardingService;
import dev.skillforge.gamification.application.GetBadgesService;
import dev.skillforge.gamification.application.GetGamifiedProfileService;
import dev.skillforge.gamification.domain.port.in.AwardXpUseCase;
import dev.skillforge.gamification.domain.port.in.BadgeAwardingUseCase;
import dev.skillforge.gamification.domain.port.in.GetBadgesUseCase;
import dev.skillforge.gamification.domain.port.in.GetGamifiedProfileUseCase;
import dev.skillforge.gamification.domain.port.out.BadgeRepository;
import dev.skillforge.gamification.domain.port.out.EarnedBadgeStore;
import dev.skillforge.gamification.domain.port.out.LevelRepository;
import dev.skillforge.gamification.domain.port.out.XpLedger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Câblage du contexte Gamification : services applicatifs purs exposés en beans. */
@Configuration
public class GamificationConfig {

    @Bean
    AwardXpUseCase awardXpUseCase(XpLedger ledger) {
        return new AwardXpService(ledger);
    }

    @Bean
    GetGamifiedProfileUseCase getGamifiedProfileUseCase(XpLedger ledger, LevelRepository levels) {
        return new GetGamifiedProfileService(ledger, levels);
    }

    @Bean
    BadgeAwardingUseCase badgeAwardingUseCase(BadgeRepository badges, EarnedBadgeStore earned, XpLedger ledger) {
        return new BadgeAwardingService(badges, earned, ledger);
    }

    @Bean
    GetBadgesUseCase getBadgesUseCase(EarnedBadgeStore earned) {
        return new GetBadgesService(earned);
    }
}
