package dev.skillforge.gamification.domain.model;

/**
 * Profil gamifié : XP cumulée, palier courant, palier suivant (nul si niveau max) et XP
 * restante avant le prochain palier.
 */
public record GamifiedProfile(
        long totalXp,
        Level currentLevel,
        Level nextLevel,
        long xpToNextLevel) {
}
