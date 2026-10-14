package dev.skillforge.catalogue.adapter.in.web;

import dev.skillforge.catalogue.adapter.in.web.dto.ImportResultDto;
import dev.skillforge.catalogue.domain.port.in.IngestRoadmapUseCase;
import dev.skillforge.catalogue.domain.port.in.RoadmapImport;
import dev.skillforge.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Adaptateur entrant WebFlux : ingestion d'une roadmap au format pivot.
 * Réservé INSTRUCTOR/ADMIN (autorisation appliquée par la SecurityWebFilterChain sur
 * {@code POST /api/v1/roadmaps/import}). Le corps est le pivot documenté (docs/PIVOT-ROADMAP.md).
 */
@RestController
@RequestMapping("/api/v1/roadmaps")
public class RoadmapImportController {

    private final IngestRoadmapUseCase ingestRoadmap;

    public RoadmapImportController(IngestRoadmapUseCase ingestRoadmap) {
        this.ingestRoadmap = ingestRoadmap;
    }

    @PostMapping("/import")
    public Mono<ResponseEntity<ApiResponse<ImportResultDto>>> importRoadmap(
            @RequestBody RoadmapImport pivot) {
        return ingestRoadmap.ingest(pivot)
                .map(id -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.ok(new ImportResultDto(id.value(), pivot.slug()))));
    }
}
