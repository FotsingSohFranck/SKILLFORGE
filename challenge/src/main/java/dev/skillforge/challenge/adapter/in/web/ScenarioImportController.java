package dev.skillforge.challenge.adapter.in.web;

import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import dev.skillforge.challenge.domain.port.in.ImportScenarioUseCase;
import dev.skillforge.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Ingestion d'un scénario de Final Challenge (pivot). Chaque import crée une nouvelle version
 * immuable. Réservé INSTRUCTOR/ADMIN (règle dans la SecurityWebFilterChain). Définition invalide
 * → 422 (code {@code INVALID_SCENARIO}) avec la liste des erreurs.
 */
@RestController
@RequestMapping("/api/v1/final-challenges/scenarios")
public class ScenarioImportController {

    private final ImportScenarioUseCase importScenario;

    public ScenarioImportController(ImportScenarioUseCase importScenario) {
        this.importScenario = importScenario;
    }

    @PostMapping("/import")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> importScenario(
            @RequestBody ScenarioDefinition pivot) {
        return importScenario.importScenario(pivot)
                .map(ref -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.ok(Map.of(
                                "scenarioId", ref.scenarioId(),
                                "version", ref.version()))));
    }
}
