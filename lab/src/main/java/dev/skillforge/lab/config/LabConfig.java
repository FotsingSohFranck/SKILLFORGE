package dev.skillforge.lab.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import dev.skillforge.lab.adapter.out.runtime.DockerCliLabRuntime;
import dev.skillforge.lab.adapter.out.runtime.DockerJavaTerminalRuntime;
import dev.skillforge.lab.application.GetPracticalService;
import dev.skillforge.lab.application.ImportPracticalTaskService;
import dev.skillforge.lab.application.LabTerminalService;
import dev.skillforge.lab.application.SubmitPracticalService;
import dev.skillforge.lab.domain.port.in.GetPracticalUseCase;
import dev.skillforge.lab.domain.port.in.ImportPracticalTaskUseCase;
import dev.skillforge.lab.domain.port.in.SubmitPracticalUseCase;
import dev.skillforge.lab.domain.port.out.LabCatalogPort;
import dev.skillforge.lab.domain.port.out.LabEventPublisher;
import dev.skillforge.lab.domain.port.out.LabRuntimePort;
import dev.skillforge.lab.domain.port.out.LabTerminalRuntime;
import dev.skillforge.lab.domain.port.out.PracticalAttemptStore;
import dev.skillforge.lab.domain.port.out.PracticalTaskStore;
import dev.skillforge.lab.domain.port.out.TerminalTicketStore;
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

    @Bean
    public GetPracticalUseCase getPracticalUseCase(PracticalTaskStore tasks,
                                                   PracticalAttemptStore attempts,
                                                   LabCatalogPort catalog) {
        return new GetPracticalService(tasks, attempts, catalog);
    }

    @Bean
    public ImportPracticalTaskUseCase importPracticalTaskUseCase(PracticalTaskStore tasks,
                                                                 LabCatalogPort catalog) {
        return new ImportPracticalTaskService(tasks, catalog);
    }

    // --- Terminal interactif (SF-91f) ---

    /**
     * Client API Docker (socket du démon). Comme la CLI, il reste inerte tant que le socket n'est
     * pas monté (cf. docker-compose.lab.yml) : la construction n'ouvre pas de connexion.
     */
    @Bean
    @ConditionalOnMissingBean(DockerClient.class)
    public DockerClient dockerClient(
            @Value("${skillforge.lab.docker-host:unix:///var/run/docker.sock}") String dockerHost) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(16)
                .connectionTimeout(Duration.ofSeconds(10))
                .responseTimeout(Duration.ZERO) // flux de terminal : pas de timeout de lecture
                .build();
        return DockerClientImpl.getInstance(config, httpClient);
    }

    @Bean
    public LabTerminalRuntime labTerminalRuntime(DockerClient dockerClient) {
        return new DockerJavaTerminalRuntime(dockerClient);
    }

    @Bean
    public LabTerminalService labTerminalService(TerminalTicketStore tickets,
                                                 PracticalTaskStore tasks,
                                                 LabRuntimePort runtime,
                                                 LabTerminalRuntime terminalRuntime,
                                                 @Value("${skillforge.lab.max-terminals:2}") int maxTerminals) {
        return new LabTerminalService(tickets, tasks, runtime, terminalRuntime, maxTerminals);
    }
}
