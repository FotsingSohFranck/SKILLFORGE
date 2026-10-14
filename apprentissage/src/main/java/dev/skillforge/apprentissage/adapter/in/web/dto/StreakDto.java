package dev.skillforge.apprentissage.adapter.in.web.dto;

import java.time.LocalDate;

/** Représentation web de la série d'apprentissage. */
public record StreakDto(
        int currentLength,
        int longestLength,
        LocalDate lastActivityDate) {
}
