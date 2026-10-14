package dev.skillforge.gamification.adapter.in.web.dto;

import java.time.Instant;

/** Représentation web d'un badge obtenu. */
public record BadgeDto(String code, String name, String description, String icon, Instant earnedAt) {
}
