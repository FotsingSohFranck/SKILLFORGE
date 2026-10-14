package dev.skillforge.catalogue.domain.port.in;

import dev.skillforge.catalogue.domain.model.RoadmapId;
import reactor.core.publisher.Mono;

/** Importer (créer ou mettre à jour) une roadmap complète à partir du pivot. */
public interface IngestRoadmapUseCase {
    Mono<RoadmapId> ingest(RoadmapImport command);
}
