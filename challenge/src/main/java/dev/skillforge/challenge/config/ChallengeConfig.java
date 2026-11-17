package dev.skillforge.challenge.config;

import dev.skillforge.challenge.application.GetScenarioService;
import dev.skillforge.challenge.application.ImportScenarioService;
import dev.skillforge.challenge.domain.port.in.GetScenarioUseCase;
import dev.skillforge.challenge.domain.port.in.ImportScenarioUseCase;
import dev.skillforge.challenge.domain.port.out.ScenarioStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Câblage du contexte Final Challenge (SF-92a-1) : services applicatifs purs exposés en beans.
 * L'adaptateur de persistance ({@code ScenarioStoreAdapter}) est un {@code @Component} scanné.
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
}
