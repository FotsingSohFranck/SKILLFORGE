package dev.skillforge.challenge.domain.port.out;

import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import dev.skillforge.challenge.domain.model.ScenarioRef;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Persistance des scénarios et de leurs versions immuables. */
public interface ScenarioStore {

    /**
     * Upsert du scénario (par {@code scenarioKey}) puis insertion d'une <strong>nouvelle version
     * publiée</strong> (numéro = max + 1) portant la définition. Ne modifie jamais une version
     * existante.
     */
    Mono<ScenarioRef> publishNewVersion(ScenarioDefinition definition);

    /** Dernière version publiée du scénario rattaché à ce nœud, ou vide. */
    Mono<ScenarioDefinition> findLatestPublishedByNode(UUID nodeId);
}
