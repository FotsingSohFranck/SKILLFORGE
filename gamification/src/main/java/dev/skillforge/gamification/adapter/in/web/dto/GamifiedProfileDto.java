package dev.skillforge.gamification.adapter.in.web.dto;

/** Représentation web du profil gamifié. */
public record GamifiedProfileDto(
        long totalXp,
        LevelDto currentLevel,
        LevelDto nextLevel,
        long xpToNextLevel) {
}
