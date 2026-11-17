package dev.skillforge.challenge.config;

import dev.skillforge.challenge.application.ChallengeAttemptService;
import dev.skillforge.challenge.application.GetScenarioService;
import dev.skillforge.challenge.application.ImportScenarioService;
import dev.skillforge.challenge.domain.port.in.GetScenarioUseCase;
import dev.skillforge.challenge.domain.port.in.ImportScenarioUseCase;
import dev.skillforge.challenge.domain.port.out.AttemptStore;
import dev.skillforge.challenge.domain.port.out.ChallengeEventPublisher;
import dev.skillforge.challenge.domain.port.out.ScenarioStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Câblage du contexte Final Challenge : services applicatifs purs exposés en beans. Les adaptateurs
 * de persistance ({@code ScenarioStoreAdapter}, {@code AttemptStoreAdapter}) sont des composants scannés.
 */
@Configuration
public class ChallengeConfig {

    @Bean
    ImportScenarioUseCase importScenarioUseCase(ScenarioStore store) {
        return new ImportScenarioService(store);
    }

    @Bean
    GetScenarioUseCase getScenarioUseCase(ScenarioStore store) {
        return new GetScenarioService(store);
    }

    /**
     * Expose le moteur de tentative. Un seul bean implémente les trois use cases (start / submit /
     * get), injecté par type pour chacun.
     */
    @Bean
    ChallengeAttemptService challengeAttemptService(ScenarioStore scenarioStore, AttemptStore attemptStore,
                                                    ChallengeEventPublisher events) {
        return new ChallengeAttemptService(scenarioStore, attemptStore, events);
    }
}
