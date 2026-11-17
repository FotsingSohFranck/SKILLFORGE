package dev.skillforge.challenge.adapter.in.web;

import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import dev.skillforge.challenge.domain.port.in.GetScenarioUseCase;
import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.common.error.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Lecture de la dernière version publiée du scénario rattaché à un nœud. Sert à vérifier/relire
 * l'authoring (SF-92a-1) ; l'exposition au flux apprenant (briefing + déroulé) relève du moteur
 * (SF-92a-2) via une session. Accès authentifié.
 */
@RestController
@RequestMapping("/api/v1/final-challenges/scenarios")
public class ScenarioController {

    private final GetScenarioUseCase getScenario;

    public ScenarioController(GetScenarioUseCase getScenario) {
        this.getScenario = getScenario;
    }

    @GetMapping("/by-node/{nodeId}")
    public Mono<ApiResponse<ScenarioDefinition>> byNode(@PathVariable UUID nodeId) {
        return getScenario.latestPublishedByNode(nodeId)
                .map(ApiResponse::ok)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "SCENARIO_NOT_FOUND", "Aucun scénario de Final Challenge publié pour ce nœud.")));
    }
}
