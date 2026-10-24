package dev.skillforge.lab.config;

import dev.skillforge.lab.adapter.out.runtime.DockerCliLabRuntime;
import dev.skillforge.lab.application.SubmitPracticalService;
import dev.skillforge.lab.domain.port.in.SubmitPracticalUseCase;
import dev.skillforge.lab.domain.port.out.LabEventPublisher;
import dev.skillforge.lab.domain.port.out.LabRuntimePort;
import dev.skillforge.lab.domain.port.out.PracticalAttemptStore;
import dev.skillforge.lab.domain.port.out.PracticalTaskStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Câblage du contexte lab (ports → implémentations).
 *
 * <p>L'orchestrateur Docker est bien instancié, mais reste <em>inerte</em> tant que le démon
 * Docker (CLI + socket) n'est pas fourni à l'exécution : sans lui, {@code start} échoue
 * proprement. On n'accorde donc aucune capacité privilégiée par défaut (cf. ADR-0008).
 */
@Configuration
public class LabConfig {

    @Bean
    @ConditionalOnMissingBean(LabRuntimePort.class)
    public LabRuntimePort labRuntimePort(
            @Value("${skillforge.lab.docker-bin:docker}") String dockerBin,
            @Value("${skillforge.lab.command-timeout-seconds:120}") long timeoutSeconds) {
        return new DockerCliLabRuntime(dockerBin, Duration.ofSeconds(timeoutSeconds));
    }

    @Bean
    public SubmitPracticalUseCase submitPracticalUseCase(PracticalTaskStore tasks,
                                                         PracticalAttemptStore attempts,
                                                         LabRuntimePort runtime,
                                                         LabEventPublisher events) {
        return new SubmitPracticalService(tasks, attempts, runtime, events);
    }
}
