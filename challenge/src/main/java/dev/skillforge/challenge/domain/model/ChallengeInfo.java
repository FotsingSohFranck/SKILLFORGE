package dev.skillforge.challenge.domain.model;

import java.util.List;

/**
 * Information apprenant sur le Final Challenge d'un nœud : disponibilité + briefing/objectif, mode
 * (formatif/certificatif) et règles annoncées avant lancement (aucune donnée de correction).
 * Sert à proposer l'entrée dans la simulation.
 */
public record ChallengeInfo(boolean available, String scenarioKey, String title,
                            String objective, String briefing, String mode, List<String> rules) {

    public static ChallengeInfo unavailable() {
        return new ChallengeInfo(false, null, null, null, null, null, List.of());
    }

    public static ChallengeInfo of(ScenarioDefinition def) {
        ChallengeMode mode = def.effectiveMode();
        return new ChallengeInfo(true, def.scenarioKey(), def.title(), def.objective(), def.briefing(),
                mode.name(), rulesFor(mode));
    }

    /** Règles annoncées avant lancement selon le mode (SF-92e). */
    public static List<String> rulesFor(ChallengeMode mode) {
        return mode == ChallengeMode.CERTIFICATIF
                ? List.of(
                        "Épreuve certificative : le score fait foi.",
                        "Consultation des cours désactivée pendant l'épreuve.",
                        "Pas de rejeu immédiat une fois la mission lancée.")
                : List.of(
                        "Mode formatif : entraînement sans enjeu.",
                        "Consultation des cours autorisée pendant la mission.",
                        "Rejeu possible à l'issue.");
    }
}
