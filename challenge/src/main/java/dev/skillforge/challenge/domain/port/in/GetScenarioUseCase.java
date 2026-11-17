package dev.skillforge.challenge.domain.port.in;

import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Lit la dernière version publiée du scénario rattaché à un nœud (vide si aucun). */
public interface GetScenarioUseCase {

    Mono<ScenarioDefinition> latestPublishedByNode(UUID nodeId);
}
