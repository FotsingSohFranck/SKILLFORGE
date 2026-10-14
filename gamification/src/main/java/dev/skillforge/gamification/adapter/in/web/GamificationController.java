package dev.skillforge.gamification.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.gamification.adapter.in.web.dto.BadgeDto;
import dev.skillforge.gamification.adapter.in.web.dto.GamifiedProfileDto;
import dev.skillforge.gamification.adapter.in.web.dto.LevelDto;
import dev.skillforge.gamification.domain.model.EarnedBadge;
import dev.skillforge.gamification.domain.model.GamifiedProfile;
import dev.skillforge.gamification.domain.model.LearnerId;
import dev.skillforge.gamification.domain.model.Level;
import dev.skillforge.gamification.domain.port.in.GetBadgesUseCase;
import dev.skillforge.gamification.domain.port.in.GetGamifiedProfileUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** Profil gamifié de l'apprenant courant (principal = userId). */
@RestController
@RequestMapping("/api/v1/gamification")
public class GamificationController {

    private final GetGamifiedProfileUseCase getProfile;
    private final GetBadgesUseCase getBadges;

    public GamificationController(GetGamifiedProfileUseCase getProfile, GetBadgesUseCase getBadges) {
        this.getProfile = getProfile;
        this.getBadges = getBadges;
    }

    @GetMapping("/me")
    public Mono<ApiResponse<GamifiedProfileDto>> me(Principal principal) {
        LearnerId learner = new LearnerId(UUID.fromString(principal.getName()));
        return getProfile.get(learner).map(GamificationController::toDto).map(ApiResponse::ok);
    }

    @GetMapping("/badges")
    public Mono<ApiResponse<List<BadgeDto>>> badges(Principal principal) {
        LearnerId learner = new LearnerId(UUID.fromString(principal.getName()));
        return getBadges.get(learner).map(GamificationController::toDto).collectList().map(ApiResponse::ok);
    }

    private static BadgeDto toDto(EarnedBadge b) {
        return new BadgeDto(b.code(), b.name(), b.description(), b.icon(), b.earnedAt());
    }

    private static GamifiedProfileDto toDto(GamifiedProfile p) {
        return new GamifiedProfileDto(
                p.totalXp(), toDto(p.currentLevel()), toDto(p.nextLevel()), p.xpToNextLevel());
    }

    private static LevelDto toDto(Level level) {
        return level == null ? null : new LevelDto(level.code(), level.name(), level.minXp());
    }
}
