package dev.skillforge.apprentissage.domain.model;

import java.time.LocalDate;

/**
 * Série d'apprentissage (streak). La transition est une fonction pure de l'état courant et
 * de la date d'activité.
 */
public record Streak(int currentLength, int longestLength, LocalDate lastActivityDate) {

    public static Streak empty() {
        return new Streak(0, 0, null);
    }

    /**
     * Enregistre une activité à la date {@code today} :
     * <ul>
     *   <li>même jour → inchangé ;</li>
     *   <li>jour suivant la dernière activité → série +1 ;</li>
     *   <li>sinon (trou ou première activité) → série remise à 1.</li>
     * </ul>
     */
    public Streak registerActivity(LocalDate today) {
        if (today.equals(lastActivityDate)) {
            return this;
        }
        int newCurrent = (lastActivityDate != null && lastActivityDate.plusDays(1).equals(today))
                ? currentLength + 1
                : 1;
        int newLongest = Math.max(longestLength, newCurrent);
        return new Streak(newCurrent, newLongest, today);
    }
}
