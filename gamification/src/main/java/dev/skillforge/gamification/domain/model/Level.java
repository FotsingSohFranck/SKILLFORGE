package dev.skillforge.gamification.domain.model;

/** Palier de niveau (barème paramétrable, table {@code levels}). */
public record Level(String code, String name, int minXp, int position) {
}
