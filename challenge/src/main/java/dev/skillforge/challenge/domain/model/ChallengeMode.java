package dev.skillforge.challenge.domain.model;

/**
 * Mode d'un Final Challenge (SF-92e). Les règles diffèrent et sont annoncées avant lancement :
 * <ul>
 *   <li>{@link #FORMATIF} : entraînement — consultation des cours autorisée pendant la mission,
 *       rejeu immédiat possible.</li>
 *   <li>{@link #CERTIFICATIF} : épreuve — consultation des cours désactivée pendant l'épreuve,
 *       le score fait foi (pas de rejeu immédiat proposé).</li>
 * </ul>
 */
public enum ChallengeMode {
    FORMATIF,
    CERTIFICATIF;

    /** Consultation des cours autorisée pendant la mission ? */
    public boolean allowsLessons() {
        return this == FORMATIF;
    }

    /** Rejeu immédiat proposé à l'issue ? */
    public boolean allowsReplay() {
        return this == FORMATIF;
    }
}
