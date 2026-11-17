package dev.skillforge.challenge.application;

import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import dev.skillforge.challenge.domain.port.in.GetScenarioUseCase;
import dev.skillforge.challenge.domain.port.out.ScenarioStore;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Lecture de la dernière version publiée d'un scénario rattaché à un nœud. */
public class GetScenarioService implements GetScenarioUseCase {

    private final ScenarioStore store;

    public GetScenarioService(ScenarioStore store) {
        this.store = store;
    }

    @Override
    public Mono<ScenarioDefinition> latestPublishedByNode(UUID nodeId) {
        return store.findLatestPublishedByNode(nodeId);
    }
}
