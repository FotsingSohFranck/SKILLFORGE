package dev.skillforge.challenge.domain.port.in;

import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import dev.skillforge.challenge.domain.model.ScenarioRef;
import reactor.core.publisher.Mono;

/**
 * Importe (valide puis publie) une définition de scénario. Chaque import d'une clé existante crée
 * une <strong>nouvelle version immuable</strong> ; les tentatives déjà liées à une version
 * antérieure ne sont pas affectées (spec §14.5 / §17.1).
 */
public interface ImportScenarioUseCase {

    Mono<ScenarioRef> importScenario(ScenarioDefinition definition);
}
