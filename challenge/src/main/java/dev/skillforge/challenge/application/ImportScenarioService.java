package dev.skillforge.challenge.application;

import dev.skillforge.challenge.domain.ScenarioValidator;
import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import dev.skillforge.challenge.domain.model.ScenarioRef;
import dev.skillforge.challenge.domain.port.in.ImportScenarioUseCase;
import dev.skillforge.challenge.domain.port.out.ScenarioStore;
import dev.skillforge.common.error.BusinessRuleException;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Valide la définition (structure + graphe) puis publie une nouvelle version immuable.
 * Définition invalide → {@link BusinessRuleException} (code {@code INVALID_SCENARIO}) → 422,
 * avec la liste des erreurs. Aucune version n'est créée tant que la validation échoue.
 */
public class ImportScenarioService implements ImportScenarioUseCase {

    private final ScenarioStore store;

    public ImportScenarioService(ScenarioStore store) {
        this.store = store;
    }

    @Override
    public Mono<ScenarioRef> importScenario(ScenarioDefinition definition) {
        List<String> errors = ScenarioValidator.validate(definition);
        if (!errors.isEmpty()) {
            return Mono.error(new BusinessRuleException("INVALID_SCENARIO", String.join(" ", errors)));
        }
        return store.publishNewVersion(definition);
    }
}
