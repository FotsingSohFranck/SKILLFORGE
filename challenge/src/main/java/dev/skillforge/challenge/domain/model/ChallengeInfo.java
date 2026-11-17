package dev.skillforge.challenge.domain.model;

/**
 * Information apprenant sur le Final Challenge d'un nœud : disponibilité + briefing/objectif
 * (aucune donnée de correction). Sert à proposer l'entrée dans la simulation.
 */
public record ChallengeInfo(boolean available, String scenarioKey, String title,
                            String objective, String briefing) {

    public static ChallengeInfo unavailable() {
        return new ChallengeInfo(false, null, null, null, null);
    }

    public static ChallengeInfo of(ScenarioDefinition def) {
        return new ChallengeInfo(true, def.scenarioKey(), def.title(), def.objective(), def.briefing());
    }
}
