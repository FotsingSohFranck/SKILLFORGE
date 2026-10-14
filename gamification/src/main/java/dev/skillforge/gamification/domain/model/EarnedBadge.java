package dev.skillforge.gamification.domain.model;

import java.time.Instant;

/** Badge obtenu par un apprenant. */
public record EarnedBadge(
        String code,
        String name,
        String description,
        String icon,
        Instant earnedAt) {
}
