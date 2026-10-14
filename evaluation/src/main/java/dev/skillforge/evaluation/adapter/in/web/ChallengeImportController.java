package dev.skillforge.evaluation.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.evaluation.domain.port.in.ChallengeImport;
import dev.skillforge.evaluation.domain.port.in.IngestChallengeUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/** Ingestion de défis (pivot). Réservé INSTRUCTOR/ADMIN (règle dans la SecurityWebFilterChain). */
@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeImportController {

    private final IngestChallengeUseCase ingest;

    public ChallengeImportController(IngestChallengeUseCase ingest) {
        this.ingest = ingest;
    }

    @PostMapping("/import")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> importChallenge(
            @RequestBody ChallengeImport pivot) {
        return ingest.ingest(pivot)
                .map(id -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.ok(Map.of("challengeId", id.value()))));
    }
}
